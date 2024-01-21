# XJson
> A simple serialization library based on Kotlin compiler plugin (K2)

Support Feature: 
- builtin primitive
- generic data class
- nested data class

Use `@XSerializable` to make the magic happen!

```kotlin
import com.icyrockton.xjson.runtime.annotation.XSerializable
import com.icyrockton.xjson.runtime.json.XJson

@XSerializable
data class Box<T>(
    val v: T
)

@XSerializable
data class Foo<T, V>(
    val a: T,
    val b: V,
    val box: Box<T>
)
```

```kotlin
fun encode() {
    val foo = Foo("hello", 20, Box("hello"))
    println(XJson { }.encodeToString(foo))
    // {"a": "hello","b": 20,"box": {"v": "hello"}}
}

fun decode() {
    val str = """
        {
           "a":"this is a",
           "b":123.456,
           "box":{
              "v":"this is v"
           }
        }
    """.trimIndent()
    println(XJson { }.decodeFromString<Foo<String, Double>>(str))
    // Foo(a=this is a, b=123.456, box=Box(v=this is v))
}
```

## Reference
[Kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

[Kotlin compiler](https://github.com/JetBrains/kotlin)