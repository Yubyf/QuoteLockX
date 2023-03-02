package com.crossbowffs.quotelock.data.modules.jinrishici.detail

import com.crossbowffs.quotelock.utils.decodeHex
import com.crossbowffs.quotelock.utils.hexString
import org.junit.Assert.assertEquals
import org.junit.Test

class JinrishiciDetailDataTest {

    private val detailData = JinrishiciDetailData(
        "小池",
        "宋代",
        "杨万里",
        listOf("泉眼无声惜细流，树阴照水爱晴柔。", "小荷才露尖尖角，早有蜻蜓立上头。"),
        listOf(
            "泉眼悄然无声是因舍不得细细的水流，树荫倒映水面是喜爱晴天和风的轻柔。",
            "娇嫩的小荷叶刚从水面露出尖尖的角，早有一只调皮的小蜻蜓立在它的上头。"
        ),
        listOf("白天")
    )

    private val byteString =
        "0100000006e5b08fe6b1a00100000006e5ae8be4bba30100000009e69da8e4b887e9878c02000000020100000030e6b389e79cbce697a0e5a3b0e6839ce7bb86e6b581efbc8ce6a091e998b4e785a7e6b0b4e788b1e699b4e69f94e380820100000030e5b08fe88db7e6898de99cb2e5b096e5b096e8a792efbc8ce697a9e69c89e89cbbe89c93e7ab8be4b88ae5a4b4e3808202000000020100000066e6b389e79cbce68284e784b6e697a0e5a3b0e698afe59ba0e8888de4b88de5be97e7bb86e7bb86e79a84e6b0b4e6b581efbc8ce6a091e88dabe58092e698a0e6b0b4e99da2e698afe5969ce788b1e699b4e5a4a9e5928ce9a38ee79a84e8bdbbe69f94e380820100000066e5a887e5aba9e79a84e5b08fe88db7e58fb6e5889ae4bb8ee6b0b4e99da2e99cb2e587bae5b096e5b096e79a84e8a792efbc8ce697a9e69c89e4b880e58faae8b083e79aaee79a84e5b08fe89cbbe89c93e7ab8be59ca8e5ae83e79a84e4b88ae5a4b4e3808202000000010100000006e799bde5a4a9"

    @Test
    fun testBytes() {
        assertEquals(byteString, detailData.bytes.hexString())
    }

    @Test
    fun testFromBytes() {
        assertEquals(detailData, JinrishiciDetailData.fromBytes(byteString.decodeHex()))
    }

    @Test
    fun testFromByteString() {
        assertEquals(detailData, JinrishiciDetailData.fromByteString(byteString))
    }
}