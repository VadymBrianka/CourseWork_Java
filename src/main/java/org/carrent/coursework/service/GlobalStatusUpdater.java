package org.carrent.coursework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

@Service
public class GlobalStatusUpdater {
    private final CarService carService;
    private final OrderService orderService;
    private final ServiceOfCarService serviceOfCarService;

    @Autowired
    public GlobalStatusUpdater(CarService carService, OrderService orderService, ServiceOfCarService serviceOfCarService) {
        this.carService = carService;
        this.orderService = orderService;
        this.serviceOfCarService = serviceOfCarService;
    }

    public void updateStatuses() {
        carService.updateCarStatuses();
        orderService.updateOrderStatuses();
        serviceOfCarService.updateServiceOfCarStatuses();
    }
}
