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
                encodeSerializableElement(descriptor, 0, String.serializer(), value.carName)
                encodeSerializableElement(descriptor, 1, Int.serializer(), value.carStock)
                encodeSerializableElement(descriptor, 2, Double.serializer(), value.price)
                encodeSerializableElement(descriptor, 3, Boolean.serializer(), value.isNewCar)
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
        val car = Car("Tesla Model Y", 200, 10.01, true)
        println(json.encodeToString(CarSerializer(),car ))
    }

    data class Food(val foodName: String, val price: Double)

    data class Animal(
        val name: String,
        val eatFood: Food
    )

    class FoodSerializer : XSerializer<Food> {
        override fun serialize(encoder: Encoder, value: Food) {
            encoder.beginStructure(descriptor) {
                encodeSerializableElement(descriptor, 0, String.serializer(), value.foodName)
                encodeSerializableElement(descriptor, 1, Double.serializer(), value.price)
            }
        }

        override fun deserialize(decoder: Decoder): Food {
            TODO("Not yet implemented")
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Food") {
                element<String>("name")
                element<Double>("price")
            }
    }

    class AnimalSerializer : XSerializer<Animal> {
        override fun serialize(encoder: Encoder, value: Animal) {
            encoder.beginStructure(descriptor) {
                encodeSerializableElement(descriptor, 0, String.serializer(), value.name)
                encodeSerializableElement(descriptor, 0, FoodSerializer(), value.eatFood)
            }
        }

        override fun deserialize(decoder: Decoder): Animal {
            TODO("Not yet implemented")
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Animal") {
                element<String>("name")
                element("eatFood", FoodSerializer().descriptor, false)
            }
    }

    @Test
    fun testComplexProperty() {
        val food = Food("meat", 20.1)
        val dog = Animal("dog", food)
        println(XJson { }.encodeToString(AnimalSerializer(), dog))
    }


    data class Box<T, V>(val t: T, val v: V)

    class BoxSerializer<T, V>(tSerializer: XSerializer<T>, vSerializer: XSerializer<V>) : XSerializer<Box<T, V>> {
        private val typeSerializer0 = tSerializer
        private val typeSerializer1 = vSerializer

        companion object {
            fun <T, V> serializer(tSerializer: XSerializer<T>, vSerializer: XSerializer<V>): BoxSerializer<T, V> {
                return BoxSerializer(tSerializer, vSerializer)
            }
        }

        override fun serialize(encoder: Encoder, value: Box<T, V>) {
            encoder.beginStructure(descriptor) {
                encodeSerializableElement(descriptor,0,typeSerializer0,value.t)
                encodeSerializableElement(descriptor,1,typeSerializer1,value.v)
            }
        }

        override fun deserialize(decoder: Decoder): Box<T, V> {
            TODO("Not yet implemented")
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Box") {
                element("t",typeSerializer0.descriptor,false)
                element("v",typeSerializer1.descriptor,false)
            }
    }

    @Test
    fun testBox() {
        val serializer = BoxSerializer(AnimalSerializer(), CarSerializer())
        val food = Food("meat", 20.1)
        val dog = Animal("dog", food)
        val car = Car("Tesla Model Y", 200, 10.01, true)
        println(XJson { }.encodeToString(serializer, Box(dog,car) ))
    }

}