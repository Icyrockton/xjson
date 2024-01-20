import com.icyrockton.xjson.runtime.XSerialization
import com.icyrockton.xjson.runtime.XSerializer
import com.icyrockton.xjson.runtime.core.serializer
import com.icyrockton.xjson.runtime.descriptor.Descriptor
import com.icyrockton.xjson.runtime.descriptor.Descriptor.Companion.UNKNOWN_ELEMENT
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
            val composite = decoder.beginStructure(descriptor)
            var carName: String? = null
            var carStock: Int? = null
            var price: Double? = null
            var isNewCar: Boolean? = null
            while (true) {
                val idx = composite.decodeElementIndex(descriptor)
                when (idx) {
                    0 -> carName = composite.decodeSerializableElement(descriptor, 0, String.serializer())
                    1 -> carStock = composite.decodeSerializableElement(descriptor, 1, Int.serializer())
                    2 -> price = composite.decodeSerializableElement(descriptor, 2, Double.serializer())
                    3 -> isNewCar = composite.decodeSerializableElement(descriptor, 2, Boolean.serializer())
                    UNKNOWN_ELEMENT -> break
                }
            }
            composite.endStructure(descriptor)
            require(carName != null)
            require(carStock != null)
            require(price != null)
            require(isNewCar != null)
            return Car(carName, carStock, price, isNewCar)
        }
    }


    @Test
    fun testEncode() {
        val json = XJson()
        val serializer = CarSerializer()
        println(serializer.descriptor)
        val car = Car("Tesla Model Y", 200, 10.01, true)
        println(json.encodeToString(CarSerializer(), car))
    }

    @Test
    fun testDecode(){
        val json = XJson()
        val serializer = CarSerializer()
        val car = json.decodeFromString<Car>(serializer,"""
            {
               "carName":"Model Y",
               "carStock":20,
               "price":10.312,
               "isNewCar":true
            }
        """.trimIndent())
        println(car)
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
            val composite = decoder.beginStructure(descriptor)
            var foodName: String? = null
            var price: Double? = null
            while (true) {
                val idx = composite.decodeElementIndex(descriptor)
                when (idx) {
                    0 -> foodName = composite.decodeSerializableElement(descriptor, 0, String.serializer())
                    1 -> price = composite.decodeSerializableElement(descriptor, 1, Double.serializer())
                    UNKNOWN_ELEMENT -> break
                }
            }
            composite.endStructure(descriptor)
            require(foodName != null)
            require(price != null)
            return Food(foodName, price)
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Food") {
                element<String>("foodName")
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
            val composite = decoder.beginStructure(descriptor)
            var name: String? = null
            var eatFood: Food? = null
            while (true) {
                val idx = composite.decodeElementIndex(descriptor)
                when (idx) {
                    0 -> name = composite.decodeSerializableElement(descriptor, 0, String.serializer())
                    1 -> eatFood = composite.decodeSerializableElement(descriptor, 1, FoodSerializer())
                    UNKNOWN_ELEMENT -> break
                }
            }
            composite.endStructure(descriptor)
            require(name != null)
            require(eatFood != null)
            return Animal(name, eatFood)
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Animal") {
                element<String>("name")
                element("eatFood", FoodSerializer().descriptor, false)
            }
    }

    @Test
    fun testEncodeComplexDataClass() {
        val food = Food("meat", 20.1)
        val dog = Animal("dog", food)
        println(XJson { }.encodeToString(AnimalSerializer(), dog))
    }

    @Test
    fun testDecodeComplexDataClass() {
        val json = XJson { }
        val dog = json.decodeFromString(AnimalSerializer() , """
            {
               "name":"Dog",
               "eatFood":{
                  "foodName":"meat",
                  "price":20.122121
               }
            }
        """.trimIndent())
        println(dog)
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
                encodeSerializableElement(descriptor, 0, typeSerializer0, value.t)
                encodeSerializableElement(descriptor, 1, typeSerializer1, value.v)
            }
        }

        override fun deserialize(decoder: Decoder): Box<T, V> {
            val composite = decoder.beginStructure(descriptor)
            var t: T? = null
            var v: V? = null
            while (true) {
                val idx = composite.decodeElementIndex(descriptor)
                when (idx) {
                    0 -> t = composite.decodeSerializableElement(descriptor, 0, typeSerializer0)
                    1 -> v = composite.decodeSerializableElement(descriptor, 1, typeSerializer1)
                    UNKNOWN_ELEMENT -> break
                }
            }
            composite.endStructure(descriptor)
            require(t != null)
            require(v != null)
            return Box(t, v)
        }

        override val descriptor: Descriptor
            get() = buildObjSerialDescriptor("Box") {
                element("t", typeSerializer0.descriptor, false)
                element("v", typeSerializer1.descriptor, false)
            }
    }

    @Test
    fun testEncodeBox() {
        val serializer = BoxSerializer(AnimalSerializer(), CarSerializer())
        val food = Food("meat", 20.1)
        val dog = Animal("dog", food)
        val car = Car("Tesla Model Y", 200, 10.01, true)
        println(XJson { }.encodeToString(serializer, Box(dog, car)))
    }

    @Test
    fun testDecodeBox() {
        val serializer = BoxSerializer(AnimalSerializer(), CarSerializer())
        val json = XJson {  }
        val box = json.decodeFromString(serializer  , """
            {
               "t":{
                  "name":"Dog",
                  "eatFood":{
                     "foodName":"meat",
                     "price":20.122121
                  }
               },
               "v":{
                  "carName":"Model Y",
                  "carStock":20,
                  "price":10.312,
                  "isNewCar":true
               }
            }
        """.trimIndent() )
        println(box)
    }

}