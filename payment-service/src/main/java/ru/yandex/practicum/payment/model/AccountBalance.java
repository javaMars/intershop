package ru.yandex.practicum.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.*;

import java.time.LocalDateTime;

@Table("account_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

    @Id
    @Column("user_id")
    private String userId;

    @Column("balance")
    private Double balance;
}