package pl.kacper.reservation.hotelReservationSystem.integrations;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import java.util.Set;

public class SchedulingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private Set<ScheduledTaskHolder> scheduledTaskHolders;

    @Test
    void shouldRegisterDailyMaintenanceCronTasks() {
        // Without @EnableScheduling no @Scheduled method would be registered at all
        var cronTasks = scheduledTaskHolders.stream()
                .flatMap(holder -> holder.getScheduledTasks().stream())
                .map(ScheduledTask::getTask)
                .filter(CronTask.class::isInstance)
                .map(CronTask.class::cast)
                .toList();

        Assertions.assertThat(cronTasks).hasSize(3);
        Assertions.assertThat(cronTasks)
                .extracting(CronTask::getExpression)
                .containsOnly("0 0 3 * * *");
    }
}
