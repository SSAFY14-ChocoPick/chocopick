// app/src/main/java/com/ssafy/chocopick/ai/HardcodedContext.kt
package com.ssafy.chocopick.ai

object HardcodedContext {

    // ✅ 매장 (하드코딩)
    private val STORES = listOf(
        StoreInfo("store_001", "초코픽 강남점", "서울", "서울특별시 강남구 테헤란로"),
        StoreInfo("store_002", "초코픽 홍대점", "서울", "서울특별시 마포구 양화로"),
        StoreInfo("store_003", "초코픽 잠실점", "서울", "서울특별시 송파구 올림픽로"),
        StoreInfo("store_004", "초코픽 수원점", "경기", "경기도 수원시 팔달구"),
        StoreInfo("store_005", "초코픽 성남점", "경기", "경기도 성남시 분당구"),
        StoreInfo("store_006", "초코픽 인천점", "인천", "인천광역시 남동구"),
        StoreInfo("store_007", "초코픽 대전 둔산점", "대전", "대전광역시 서구 둔산동"),
        StoreInfo("store_008", "초코픽 광주 상무점", "광주", "광주광역시 서구 치평동"),
        StoreInfo("store_009", "초코픽 대구 동성로점", "대구", "대구광역시 중구 동성로"),
        StoreInfo("store_010", "초코픽 부산 서면점", "부산", "부산광역시 부산진구 서면"),
        StoreInfo("store_011", "초코픽 부산 해운대점", "부산", "부산광역시 해운대구"),
        StoreInfo("store_012", "초코픽 울산 삼산점", "울산", "울산광역시 남구 삼산동"),
        StoreInfo("store_013", "초코픽 창원 상남점", "경남", "경상남도 창원시 성산구 상남동"),
        StoreInfo("store_014", "초코픽 포항 북구점", "경북", "경상북도 포항시 북구"),
        StoreInfo("store_015", "초코픽 구미 인동점", "경북", "경상북도 구미시 인동"),
        StoreInfo("store_016", "초코픽 구미 진평점", "경북", "경상북도 구미시 진평동"),
        StoreInfo("store_017", "초코픽 전주 한옥마을점", "전북", "전라북도 전주시 완산구"),
        StoreInfo("store_018", "초코픽 춘천점", "강원", "강원도 춘천시 중앙로"),
        StoreInfo("store_019", "초코픽 청주 성안길점", "충북", "충청북도 청주시 상당구"),
        StoreInfo("store_020", "초코픽 제주 연동점", "제주", "제주특별자치도 제주시 연동"),
    )

    // ✅ 상품 (하드코딩)
    private val PRODUCTS = listOf(
        ProductInfo("롯데 ABC초코렛 565G", 13990),
        ProductInfo("매일 킨더 초콜릿 T-4 50G", 2100),
        ProductInfo("매일 페레로로쉐 T8 하트 100G", 8640),
        ProductInfo("롯데 드림카카오 82% GABA 86G", 3990),
        ProductInfo("엠앤엠즈 펀사이즈 밀크 230G", 6290),
        ProductInfo("린트 엑설런스 다크 70% 100G", 6390),
        ProductInfo("롯데 아몬드 초코볼 46G", 2090),
        ProductInfo("스니커즈 미니스 1020G", 24990),
        ProductInfo("휘태커스 미니슬랩 피넛 초콜릿 180G", 11990),
        ProductInfo("몰티져스 밀크버켓 초콜릿 465G", 14990),
        ProductInfo("롯데 미니 가나마일드 175G", 6790),
        ProductInfo("허쉬 뉴키세스 쿠키앤크림 146G", 5290),
        ProductInfo("밀카 요거트 초콜릿 100G", 4480),
        ProductInfo("롯데 제로 크런치 초코볼 140G", 4990),
        ProductInfo("리터 콘플레이크 초콜릿 100G", 5890),
        ProductInfo("린트 스위스씬 밀크 초콜릿 125G", 15990),
        ProductInfo("롯데 석기시대 90G", 3590),
        ProductInfo("해태 얼초 동물그리기 52G", 2580),
        ProductInfo("골든씨 씨쉘 초콜릿 125G", 11990),
    )

    data class StoreInfo(
        val id: String,
        val name: String,
        val region: String,
        val address: String
    )

    data class ProductInfo(
        val name: String,
        val price: Int
    )

    fun allProductNamesText(): String =
        PRODUCTS.mapIndexed { i, p -> "${i + 1}. ${p.name}" }.joinToString("\n")

    fun allProductPricesText(): String =
        PRODUCTS.mapIndexed { i, p -> "${i + 1}. ${p.name} - ${p.price}원" }.joinToString("\n")

    fun allStoresText(): String =
        STORES.mapIndexed { i, s -> "${i + 1}. ${s.name} - ${s.address}" }.joinToString("\n")

    fun findStoreAddress(query: String): String? {
        val q = query.replace(" ", "")
        val store = STORES.firstOrNull { s ->
            val name = s.name.replace(" ", "")
            name.contains(q) || q.contains(name) || name.contains(q.replace("주소", ""))
        }
        return store?.address
    }
}
