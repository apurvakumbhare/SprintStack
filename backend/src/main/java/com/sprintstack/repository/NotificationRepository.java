package com.sprintstack.repository;
import com.sprintstack.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
public interface NotificationRepository extends JpaRepository<Notification,Long>{
 List<Notification> findTop50ByUser_IdOrderByCreatedAtDesc(Long userId);
 List<Notification> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);
 Optional<Notification> findByIdAndUser_Id(Long id,Long userId);
 long countByUser_IdAndReadFalse(Long userId);
 boolean existsByUser_IdAndTypeAndRelatedEntityIdAndCreatedAtAfter(Long userId,String type,Long entityId,LocalDateTime after);
 @Modifying @Query("update Notification n set n.read = true where n.user.id = :userId and n.read = false") int markAllRead(Long userId);
}
