package com.ssafy.chocopick.chocopick.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class DelayedFcmService {

  private final ThreadPoolTaskScheduler scheduler;
  private final FCMService fcmService;

  public DelayedFcmService(ThreadPoolTaskScheduler scheduler, FCMService fcmService) {
    this.scheduler = scheduler;
    this.fcmService = fcmService;
  }

  public void sendNowAndDelayed(String token) {
    // 0초: 주문 완료
    safeSend(token, "주문 완료!", "주문이 완료되었습니다.");

    // 10초: 주문 접수
    schedule(() -> safeSend(token, "주문 접수!", "매장에서 주문을 확인했어요."), 10);

    // 20초: 픽업 안내
    schedule(() -> safeSend(token, "픽업하세요!", "지금 매장에서 픽업할 수 있어요."), 20);
  }

  private void schedule(Runnable task, int secondsLater) {
    Instant runAt = Instant.now().plusSeconds(secondsLater);
    scheduler.schedule(task, Date.from(runAt));
  }

  private void safeSend(String token, String title, String body) {
    try {
      String messageId = fcmService.sendToToken(token, title, body);
      System.out.println("[FCM] sent title=" + title + " messageId=" + messageId);
    } catch (Exception e) {
      System.out.println("[FCM] failed title=" + title + " err=" + e.getMessage());
    }
  }
  
  public void sendStoreOrderNowAndDelayed(String token, int tableNo) {
	    safeSend(token, tableNo + "번 테이블 주문 접수", tableNo + "번 테이블 주문이 접수되었습니다.");

	    schedule(() -> safeSend(
	            token,
	            tableNo + "번 테이블 고객님",
	            "초콜릿이 준비되었습니다."
	    ), 10);
	}
}