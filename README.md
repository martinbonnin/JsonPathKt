# JsonPathKt
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.eygraber/jsonpathkt/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.eygraber/jsonpathkt)

**A lighter and more efficient implementation of JsonPath in Kotlin.**
With functional programming aspects found in langauges like Kotlin, Scala, and streams/lambdas in Java8, this
library simplifies other implementations like [Jayway's JsonPath](https://github.com/json-path/JsonPath) by removing 
*filter operations* and *in-path functions* to focus on what matters most: modern fast value extractions from JSON objects. 
Up to **4x more efficient** in some cases; see [Benchmarks](#benchmarks).

In order to make the library functional programming friendly, JsonPathKt returns `null` instead of throwing exceptions 
while evaluating a path against a JSON object. Throwing exceptions breaks flow control and should be reserved for exceptional 
errors only.

## Code examples
A jsonpath that exists returns that value. `null` is returned when it doesn't.
```kotlin
val json = """{"hello": "world"}"""
Json.parseToJsonElement(json)?.read<String>("$.hello") // returns "world"
Json.parseToJsonElement(json)?.read<String>("$.somethingelse") // returns null since "somethingelse" key not found
```

A jsonpath that returns a collection containing the 2nd and 3rd items in the list (index 0 based and exclusive at range end).
```kotlin
val json = """{"list": ["a","b","c","d"]}"""
Json.parseToJsonElement(json)?.read<List<String>>("$.list[1:3]") // returns listOf("b", "c")
```

JsonPathKt also works with `Map` and POJO.
```kotlin
val json = """[{ "outer": {"inner": 1} }]"""
Json.parseToJsonElement(json)?.read<Map<String, Int>>("$[0].outer") // returns mapOf("inner" to 1)
data class ParsedResult(val outer: Map<String, Int>) // define this class in file scope, not in function scope which will anonymize it 
Json.parseToJsonElement(json)?.read<ParsedResult>("$[0]") // returns ParsedResult instance
```

Internally, a jsonpath is compiled into a list of tokens. You can compile a complex jsonpath once and reuse it across multiple JSON strings.
```kotlin
val jsonpath = JsonPath("$.family.children..['name','nickname']")
jsonpath.readFromJson<List<Map<String, String>>>(json1)
jsonpath.readFromJson<List<Map<String, String>>>(json2)
```

*JsonPathKt uses [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) to deserialize JSON strings. `Json.parseToJsonElement` returns a 
`JsonElement` object, so if you've already deserialized, you can also `read` the jsonpath value directly.*


## Getting started
JsonPathKt is available at the Maven Central repository.

**POM**
```xml
<dependency>
  <groupId>com.nfeld.jsonpathkt</groupId>
  <artifactId>jsonpathkt</artifactId>
  <version>2.0.0</version>
</dependency>
```

**Gradle**
```gradle
dependencies {
    implementation("com.nfeld.jsonpathkt:jsonpathkt:2.0.0")
}
```

## Accessor operators

| Operator                  | Description                                                          |
|:--------------------------|:---------------------------------------------------------------------|
| `$`                       | The root element to query. This begins all path expressions.         |
| `..`                      | Deep scan for values behind followed key value accessor              |
| `.<name>`                 | Dot-notated key value accessor for JSON objects                      |
| `['<name>' (, '<name>')]` | Bracket-notated key value accessor for JSON objects, comma-delimited |
| `[<number> (, <number>)]` | JSON array accessor for index or comma-delimited indices             |
| `[start:end]`             | JSON array range accessor from start (inclusive) to end (exclusive)  |

## Path expression examples
JsonPathKt expressions can use any combination of dot–notation and bracket–notation operators to access JSON values. For examples, these all evaluate to the same result:
```text
$.family.children[0].name
$['family']['children'][0]['name']
$['family'].children[0].name
```

Given the JSON:
```json
{
    "family": {
        "children": [{
                "name": "Thomas",
                "age": 13
            },
            {
                "name": "Mila",
                "age": 18
            },
            {
                "name": "Konstantin",
                "age": 29,
                "nickname": "Kons"
            },
            {
                "name": "Tracy",
                "age": 4
            }
        ]
    }
}
```

| JsonPath                   | Result                                     |
|:---------------------------|:-------------------------------------------|
| $.family                   | The family object                          |
| $.family.children          | The children array                         |
| $.family['children']       | The children array                         |
| $.family.children[2]       | The second child object                    |
| $.family.children[-1]      | The last child object                      |
| $.family.children[-3]      | The 3rd to last child object               |
| $.family.children[1:3]     | The 2nd and 3rd children objects           |
| $.family.children[:3]      | The first three children                   |
| $.family.children[:-1]     | The first three children                   |
| $.family.children[2:]      | The last two children                      |
| $.family.children[-2:]     | The last two children                      |
| $..name                    | All names                                  |
| $.family..name             | All names nested within family object      |
| $.family.children[:3]..age | The ages of first three children           |
| $..['name','nickname']     | Names & nicknames (if any) of all children |
| $.family.children[0].*     | Names & age values of first child          |

## Benchmarks
These are benchmark tests of JsonPathKt JVM against Jayway's JsonPath implementation. Results for each test is the average of 
30 runs with 80,000 reads per run and each test returns its own respective results (some larger than others).
You can run these tests locally with `./runBenchmarks.sh`

**Evaluating/reading path against large JSON**

| Path Tested                             | JsonPathKt (ms) | JsonPath (ms) |
|:----------------------------------------|:----------------|:--------------|
| $[0].friends[1].other.a.b['c']          | 26 ms           | 53 ms         |
| $[2]._id                                | 7 ms            | 18 ms         |
| $..name                                 | 43 ms           | 275 ms        |
| $..['email','name']                     | 52 ms           | 268 ms        |
| $..[1]                                  | 33 ms           | 261 ms        |
| $..[:2]                                 | 40 ms           | 274 ms        |
| $..[2:]                                 | 59 ms           | 286 ms        |
| $..[1:-1]                               | 58 ms           | 248 ms        |
| $[0]['tags'][-3]                        | 13 ms           | 32 ms         |
| $[0]['tags'][:3]                        | 19 ms           | 40 ms         |
| $[0]['tags'][3:]                        | 20 ms           | 46 ms         |
| $[0]['tags'][3:5]                       | 21 ms           | 41 ms         |
| $[0]['tags'][0,3,5]                     | 23 ms           | 48 ms         |
| $[0]['latitude','longitude','isActive'] | 22 ms           | 67 ms         |
| $[0]['tags'].*                          | 11 ms           | 50 ms         |
| $[0]..*                                 | 62 ms           | 476 ms        |



**Compiling JsonPath strings to internal tokens**

| Path size           | JsonPathKt | JsonPath |
|:--------------------|:-----------|:---------|
| 7 chars, 1 tokens   | 3 ms       | 2 ms     |
| 16 chars, 3 tokens  | 8 ms       | 8 ms     |
| 30 chars, 7 tokens  | 15 ms      | 20 ms    |
| 65 chars, 16 tokens | 35 ms      | 49 ms    |
| 88 chars, 19 tokens | 45 ms      | 70 ms    |


# Cache
JsonPathKt doesn't provide a caching layer anymore. If caching is desired, there are multiple
KMP caching libraries that can be used to wrap JsonPathKt.
