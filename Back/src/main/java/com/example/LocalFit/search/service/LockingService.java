package com.example.LocalFit.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockingService {

    private final RedissonClient redisson;

    public boolean isLockAcquired(String lockName) {
        RLock lock = redisson.getLock(lockName);
        boolean isLocked = lock.isLocked();
        log.info("락 점유 상태 확인 - 락 이름: {}, 점유 상태: {}", lockName, isLocked);
        return isLocked;
    }

    public void releaseLock(String lockName) {
        RLock lock = redisson.getLock(lockName);
        lock.forceUnlock();
        log.info("락이 해제되었습니다 - 락 이름: {}", lockName);
    }

}
