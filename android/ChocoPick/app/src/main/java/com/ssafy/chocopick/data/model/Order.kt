package com.ssafy.chocopick.data.model

data class Order(
    val orderId: String = "",          // Firebase key
    val items: List<OrderItem> = emptyList(),
    val orderDate: Long = 0L,
    val status: String = "",           // "주문 완료"
    val store: String = "",            // storeId (ex. "store_006")
    val totalPrice: Int = 0,
    val uid: String = "",
    val orderType : String = "PICKUP",
    val tableNo : Int? = null
)

//Order 모델에 orderType, tableNo 추가해둠.

/*
CartFragment에서 혹은 다른 곳 어딧던 Order 모델을 호출,참조 하느느 곳에서는 인자를 추가로 작성해줘야함.
CartFragment에서 매장 주문 클릭시 nfc를 태깅하라는 다이얼로그를 띄운 상태에서 nfc를 태깅하면 현재 장바구니에 담긴 상품을 바로 주문하는 방식으로 구현하기
주문 성공시, 무조건 1번 테이블 주문 접수 완료, 1번 테이블 고객님 초콜릿이 준비되었습니다. 라는 알림 fcm 추가 구현해야함.


2. 주문 하기 누르면 스탬프 적립 기능 구현, 쿠폰 어떻게 사용 할지 생각해보기.
 */