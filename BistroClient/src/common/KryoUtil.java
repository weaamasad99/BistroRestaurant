package common;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Time;

/**
 * Utility class for handling Kryo serialization.
 * Ensures consistent registration of classes across Client and Server.
 */
public class KryoUtil {

    // Kryo is not thread-safe, so we use ThreadLocal to ensure each thread has its own instance.
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        
        // --- Registration ---
        // It is critical to register classes in the EXACT SAME ORDER on both client and server.
        kryo.register(Message.class);
        kryo.register(TaskType.class);
        kryo.register(Order.class);
        kryo.register(ArrayList.class);
        kryo.register(Date.class);
        kryo.register(User.class);
        kryo.register(Time.class);
        // Add any other classes you intend to send here
        
        return kryo;
    });

    /**
     * Serializes an object into a byte array.
     * @param object The object to serialize.
     * @return The byte array representing the object.
     */
    public static byte[] serialize(Object object) {
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeClassAndObject(output, object);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserializes a byte array back into an object.
     * @param bytes The byte array to deserialize.
     * @return The reconstructed object.
     */
    public static Object deserialize(byte[] bytes) {
        if (bytes == null) return null;
        Kryo kryo = kryoThreadLocal.get();
        Input input = new Input(new ByteArrayInputStream(bytes));
        return kryo.readClassAndObject(input);
    }
}