package com.idp.idpapi.notification.entity;

import java.time.LocalDateTime;

import com.idp.idpapi.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_settings")
public class NotificationSetting {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = Boolean.TRUE;

    @Column(name = "system_notification", nullable = false)
    private Boolean systemNotification = Boolean.TRUE;

    @Column(name = "contract_new", nullable = false)
    private Boolean contractNew = Boolean.TRUE;

    @Column(name = "contract_approval", nullable = false)
    private Boolean contractApproval = Boolean.TRUE;

    @Column(name = "contract_expiring", nullable = false)
    private Boolean contractExpiring = Boolean.TRUE;

    @Column(name = "comment_mention", nullable = false)
    private Boolean commentMention = Boolean.TRUE;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public Boolean getSystemNotification() {
        return systemNotification;
    }

    public void setSystemNotification(Boolean systemNotification) {
        this.systemNotification = systemNotification;
    }

    public Boolean getContractNew() {
        return contractNew;
    }

    public void setContractNew(Boolean contractNew) {
        this.contractNew = contractNew;
    }

    public Boolean getContractApproval() {
        return contractApproval;
    }

    public void setContractApproval(Boolean contractApproval) {
        this.contractApproval = contractApproval;
    }

    public Boolean getContractExpiring() {
        return contractExpiring;
    }

    public void setContractExpiring(Boolean contractExpiring) {
        this.contractExpiring = contractExpiring;
    }

    public Boolean getCommentMention() {
        return commentMention;
    }

    public void setCommentMention(Boolean commentMention) {
        this.commentMention = commentMention;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
