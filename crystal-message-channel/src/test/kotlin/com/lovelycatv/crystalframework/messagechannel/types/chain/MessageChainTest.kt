package com.lovelycatv.crystalframework.messagechannel.types.chain

import com.lovelycatv.crystalframework.messagechannel.types.chain.dsl.messageChain
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageChainTest {

    @Test
    fun dslBuildsExpectedSegments() {
        val chain = messageChain {
            text("Hello ")
            at(userId = "123", tenantId = "456", displayName = "张三")
            text(", check this image: ")
            imageByUrl("https://example.com/a.png")
            newline()
            link(url = "https://example.com", title = "homepage")
        }

        printlnSection("dslBuildsExpectedSegments")
        chain.segments.forEachIndexed { i, s -> println("  [$i] $s") }

        assertEquals(
            listOf(
                TextSegment("Hello "),
                AtSegment(userId = "123", tenantId = "456", displayName = "张三"),
                TextSegment(", check this image: "),
                ImageSegment(UrlImageSource("https://example.com/a.png")),
                NewlineSegment,
                LinkSegment(url = "https://example.com", title = "homepage"),
            ),
            chain.segments,
        )
    }

    @Test
    fun dslOmitsEmptyTextSegments() {
        val chain = messageChain {
            text("")
            text("real")
        }

        printlnSection("dslOmitsEmptyTextSegments")
        println("  segments=${chain.segments}")

        assertEquals(listOf(TextSegment("real")), chain.segments)
    }

    @Test
    fun writesAllSegmentTypesToXml() {
        val chain = messageChain {
            text("Hello ")
            at(userId = "123", tenantId = "456", displayName = "张三")
            text("!")
            newline()
            imageByUrl("https://example.com/a.png")
            imageByResource("file-001")
            link(url = "https://example.com", title = "home")
        }

        val xml = chain.toXml()

        printlnSection("writesAllSegmentTypesToXml")
        println("  chain  -> $chain")
        println("  toXml  -> $xml")

        assertEquals(
            """Hello <at user="123" tenant="456" name="张三"/>!""" +
                """<br/><image src="https://example.com/a.png"/>""" +
                """<image src="resource:file-001"/>""" +
                """<link href="https://example.com" title="home"/>""",
            xml,
        )
    }

    @Test
    fun escapesSpecialCharsInTextAndAttributes() {
        val chain = messageChain {
            text("a < b & c > d")
            link(url = "https://example.com?x=1&y=2", title = "a\"b")
        }

        val xml = chain.toXml()
        val roundTripped = MessageChain.parse(xml)

        printlnSection("escapesSpecialCharsInTextAndAttributes")
        println("  toXml         -> $xml")
        println("  parsed back   -> ${roundTripped.segments}")

        assertEquals(
            """a &lt; b &amp; c &gt; d<link href="https://example.com?x=1&amp;y=2" title="a&quot;b"/>""",
            xml,
        )
        assertEquals(chain, roundTripped)
    }

    @Test
    fun parsesXmlIntoExpectedSegments() {
        val xml = """Hello <at user="123" tenant="456" name="Zhang San"/>, see <image src="https://example.com/a.png"/><br/><link href="https://example.com" title="home"/>"""

        val chain = MessageChain.parse(xml)

        printlnSection("parsesXmlIntoExpectedSegments")
        println("  input  -> $xml")
        chain.segments.forEachIndexed { i, s -> println("  [$i] $s") }

        assertEquals(
            listOf(
                TextSegment("Hello "),
                AtSegment(userId = "123", tenantId = "456", displayName = "Zhang San"),
                TextSegment(", see "),
                ImageSegment(UrlImageSource("https://example.com/a.png")),
                NewlineSegment,
                LinkSegment(url = "https://example.com", title = "home"),
            ),
            chain.segments,
        )
    }

    @Test
    fun parsesResourceImageSourceFromXml() {
        val xml = """<image src="resource:abc-123"/>"""
        val chain = MessageChain.parse(xml)

        printlnSection("parsesResourceImageSourceFromXml")
        println("  input  -> $xml")
        println("  parsed -> ${chain.segments}")

        assertEquals(
            listOf(ImageSegment(ResourceImageSource("abc-123"))),
            chain.segments,
        )
    }

    @Test
    fun unknownTagsArePreservedAsText() {
        val xml = "hello <unknown attr=\"x\"/> world"
        val chain = MessageChain.parse(xml)

        printlnSection("unknownTagsArePreservedAsText")
        println("  input  -> $xml")
        println("  parsed -> ${chain.segments}")

        assertEquals(
            listOf(TextSegment("hello <unknown attr=\"x\"/> world")),
            chain.segments,
        )
    }

    @Test
    fun emptyAndBlankInputsProduceEmptyChain() {
        val chain = MessageChain.parse("")

        printlnSection("emptyAndBlankInputsProduceEmptyChain")
        println("  segments=${chain.segments}")

        assertTrue(chain.segments.isEmpty())
        assertEquals(MessageChain.EMPTY, chain)
    }

    @Test
    fun roundTripDslWriteParseProducesEqualChain() {
        val original = messageChain {
            text("你好 ")
            at(userId = "u-1", tenantId = "t-1", displayName = "张三")
            text("，请查看 ")
            link(url = "https://example.com?a=1&b=2", title = "详情")
            newline()
            imageByUrl("https://example.com/img.png")
            imageByResource("res-42")
        }

        val xml = original.toXml()
        val parsed = MessageChain.parse(xml)
        val xmlAgain = parsed.toXml()

        printlnSection("roundTripDslWriteParseProducesEqualChain")
        println("  original.segments -> ${original.segments}")
        println("  toXml             -> $xml")
        println("  parsed.segments   -> ${parsed.segments}")
        println("  parsed.toXml      -> $xmlAgain")

        assertEquals(original, parsed)
        assertEquals(xml, xmlAgain)
    }

    @Test
    fun chainPlusOperatorAppendsSegment() {
        val base = messageChain { text("a") }
        val combined = base + TextSegment("b")

        printlnSection("chainPlusOperatorAppendsSegment")
        println("  base     -> ${base.segments}")
        println("  combined -> ${combined.segments}")

        assertEquals(listOf(TextSegment("a"), TextSegment("b")), combined.segments)
        // original is not mutated
        assertEquals(listOf(TextSegment("a")), base.segments)
    }

    private fun printlnSection(name: String) {
        println()
        println("===== $name =====")
    }
}
