package com.ssafy.chocopick.chocopick.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.chocopick.chocopick.service.DelayedFcmService;
import com.ssafy.chocopick.chocopick.service.FCMService;

import dto.FCMRequest;

//@RestController
//@RequestMapping("/api/test")
//public class FCMController {
//
//  private final DelayedFcmService delayedFcmService;
//
//  public FCMController(DelayedFcmService delayedFcmService) {
//    this.delayedFcmService = delayedFcmService;
//  }
//
//  // POST /api/test/fcm/delayed?token=XXX
//  @PostMapping("/fcm/delayed")
//  public ResponseEntity<?> sendDelayed(@RequestParam String token) {
//    delayedFcmService.sendNowAndDelayed(token);
//    return ResponseEntity.ok("OK scheduled (0s/10s/20s)");
//  }
//}
//@RestController
//@RequestMapping("/api/test")
//public class FCMController {
//
//    private final FCMService fcmService;
//
//    public FCMController(FCMService fcmService) {
//        this.fcmService = fcmService;
//    }
//
//    @PostMapping("/fcm")
//    public ResponseEntity<?> sendTest(@RequestBody FCMRequest req) {
//        try {
//            String messageId = fcmService.sendToToken(req.getToken(), req.getTitle(), req.getBody());
//            return ResponseEntity.ok("OK messageId=" + messageId);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("FAIL " + e.getMessage());
//        }
//    }
//}

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.chocopick.chocopick.service.DelayedFcmService;
import com.ssafy.chocopick.chocopick.service.FCMService;

import dto.FCMRequest;

@RestController
@RequestMapping("/api/test")
public class FCMController {

    private final FCMService fcmService;
    private final DelayedFcmService delayedFcmService;

    public FCMController(FCMService fcmService, DelayedFcmService delayedFcmService) {
        this.fcmService = fcmService;
        this.delayedFcmService = delayedFcmService;
    }

    // 즉시 1회 전송 (JSON)
    @PostMapping("/fcm")
    public ResponseEntity<?> sendTest(@RequestBody FCMRequest req) {
        try {
            String messageId = fcmService.sendToToken(req.getToken(), req.getTitle(), req.getBody());
            return ResponseEntity.ok("OK messageId=" + messageId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("FAIL " + e.getMessage());
        }
    }

    // ✅ 0/10/20초 지연 전송 (JSON)
    @PostMapping("/fcm/delayed")
    public ResponseEntity<?> sendDelayed(@RequestBody FCMRequest req) {
        delayedFcmService.sendNowAndDelayed(req.getToken());
        return ResponseEntity.ok("OK scheduled (0s/10s/20s)");
    }
    
    @PostMapping("/fcm/storeDelayed")
    public ResponseEntity<?> sendStoreDelayed(@RequestBody FCMRequest req) {
        delayedFcmService.sendStoreOrderNowAndDelayed(req.getToken(), 1);
        return ResponseEntity.ok("OK store scheduled");
    }
}




/*curl -X POST "http://localhost:8080/api/test/fcm/delayed" ^
-H "Content-Type: application/json" ^
-d "{\"token\":\"eyapnsnwRNyEwF6JFlxuNN:APA91bEZs6N3_Wo425dWmTCbYOw1a47v7RXyNkIUW4iSfdK8Igf6e3jZZNX_nM-Gwbk9F607Dcpr9aISLWRLbhHZxs48YODFDufYMBaU4OM8QGJHHR8ilyI\",\"title\":\"주문완료\",\"body\":\"테스트입니다\"}"
*/