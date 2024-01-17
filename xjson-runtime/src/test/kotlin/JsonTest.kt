import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.XSerializer
import com.icyrockton.xjson.runtime.core.serializer
import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.descriptor.buildObjSerialDescriptor
import com.icyrockton.xjson.runtime.descriptor.buildSerialDescriptor
import com.icyrockton.xjson.runtime.encoding.Decoder
import com.icyrockton.xjson.runtime.encoding.Encoder
import com.icyrockton.xjson.runtime.encoding.beginStructure
import com.icyrockton.xjson.runtime.json.XJson
import org.junit.jupiter.api.Test

class JsonTest {

    data class Car(
        val carName: String,
        val carStock: Int,
        val price: Double,
        val isNewCar: Boolean,
    )

    class CarSerializer : XSerializer<Car> {
        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("car") {
                element<String>("carName")
                element<Int>("carStock")
                element<Double>("price")
                element<Boolean>("isNewCar")
            }

        override fun serialize(encoder: Encoder, value: Car) {
            encoder.beginStructure(descriptor) {
                encodeSerializableElement(descriptor,0 , String.serializer(), value.carName)
                encodeSerializableElement(descriptor,1 , Int.serializer(), value.carStock)
                encodeSerializableElement(descriptor,2 , Double.serializer(), value.price)
                encodeSerializableElement(descriptor,3 , Boolean.serializer(), value.isNewCar)
            }
        }

        override fun deserialize(decoder: Decoder): Car {
            TODO("Not yet implemented")
        }
    }


    @Test
    fun testEncode() {
        val json = XJson()
        val serializer = CarSerializer()
        println(serializer.descriptor)
        println(json.encodeToString(CarSerializer(), Car("Tesla Model Y", 200, 10.01, true)))
    }

}