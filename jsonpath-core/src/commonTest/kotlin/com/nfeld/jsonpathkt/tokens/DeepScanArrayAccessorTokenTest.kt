package com.nfeld.jsonpathkt.tokens

import com.nfeld.jsonpathkt.FAMILY_JSON
import com.nfeld.jsonpathkt.asJson
import com.nfeld.jsonpathkt.jsonNode
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DeepScanArrayAccessorTokenTest {
  @Test
  fun should_scan_for_indices() {
    DeepScanArrayAccessorToken(listOf(0)).read(FAMILY_JSON.asJson.jsonNode()).asJson.toString() shouldBe """[{"name":"Thomas","age":13}]"""
    DeepScanArrayAccessorToken(
      listOf(
        0,
        2,
      ),
    ).read(FAMILY_JSON.asJson.jsonNode()).asJson.toString() shouldBe """[{"name":"Thomas","age":13},{"name":"Konstantin","age":29,"nickname":"Kons"}]"""
  }

  @Test
  fun results_should_be_a_New_Root() {
    DeepScanArrayAccessorToken(listOf(0)).read(FAMILY_JSON.asJson.jsonNode()).isWildcardScope shouldBe true
  }

  @Test
  fun should_handle_different_nested_lists() {
    val json = """[ {"a":1}, {"b":2}, [0,1,2, [ true, false ]] ]""".asJson
    DeepScanArrayAccessorToken(listOf(0)).read(json.jsonNode()).asJson.toString() shouldBe """[{"a":1},0,true]"""
    DeepScanArrayAccessorToken(
      listOf(
        0,
        1,
      ),
    ).read(json.jsonNode()).asJson.toString() shouldBe """[{"a":1},{"b":2},0,1,true,false]"""

    DeepScanArrayAccessorToken(listOf(0)).read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode()).asJson.toString() shouldBe "[1,2,3,5]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        1,
      ),
    ).read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode()).asJson.toString() shouldBe "[1,[2],2,3,4,5,6]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        -1,
      ),
    ).read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode()).asJson.toString() shouldBe "[1,[5,6,7],2,2,3,4,5,7]"
    DeepScanArrayAccessorToken(listOf(0)).read(WildcardToken().read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode())).asJson.toString() shouldBe "[2,3,5]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        1,
      ),
    ).read(WildcardToken().read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode())).asJson.toString() shouldBe "[2,3,4,5,6]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        -1,
      ),
    ).read(WildcardToken().read("""[1,[2],[3,4],[5,6,7]]""".asJson.jsonNode())).asJson.toString() shouldBe "[2,2,3,4,5,7]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        1,
      ),
    ).read(WildcardToken().read("""[1,[2],[3,4],[5,6,7,[8,9,10,11]]]""".asJson.jsonNode())).asJson.toString() shouldBe "[2,3,4,5,6,8,9]"
    DeepScanArrayAccessorToken(
      listOf(
        0,
        -1,
      ),
    ).read(WildcardToken().read("""[1,[2],[3,4],[5,6,7,[8,9,10,11]]]""".asJson.jsonNode())).asJson.toString() shouldBe "[2,2,3,4,5,[8,9,10,11],8,11]"
  }
}
