package org.carrent.coursework.enums;

public enum OrderStatus {
    RESERVED,        // Бронювання створено і підтверджено
    ACTIVE,          // Бронювання активне, автомобіль видано клієнту
    COMPLETED,       // Бронювання завершено, автомобіль повернуто
    CANCELED         // Бронювання скасовано
}
