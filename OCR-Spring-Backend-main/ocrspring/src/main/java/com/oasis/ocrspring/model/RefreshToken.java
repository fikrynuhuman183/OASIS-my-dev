package com.oasis.ocrspring.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "refreshtokens")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RefreshToken {
    @Id
    @Field("_id")
    private ObjectId id;

    private ObjectId user;

    private String token;

    private LocalDateTime expiresAt;

    private String createdByIP;

    private LocalDateTime createdAt;

    private LocalDateTime revokedAt;

    private String revokedByIP;

    private String replacedByToken;

    public RefreshToken(ObjectId user, String token, String createdByIP,
                        LocalDateTime revokedAt, String revokedByIP,
                        String replacedByToken) {
        this.user = user;
        this.token = token;
        this.createdByIP = createdByIP;
        this.revokedAt = revokedAt;
        this.revokedByIP = revokedByIP;
        this.replacedByToken = replacedByToken;
    }

    public boolean isExpired() {
        return this.expiresAt.isBefore(LocalDateTime.now());
    }
}