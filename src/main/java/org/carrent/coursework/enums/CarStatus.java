package org.carrent.coursework.enums;

public enum CarStatus {
    AVAILABLE,     // Автомобіль доступний для оренди
    RENTED,        // Автомобіль в оренді
    IN_SERVICE,    // Автомобіль на обслуговуванні
    RESERVED,      // Автомобіль заброньовано, але ще не видано
    OUT_OF_ORDER   // Автомобіль тимчасово недоступний (наприклад, через поломку)
}
