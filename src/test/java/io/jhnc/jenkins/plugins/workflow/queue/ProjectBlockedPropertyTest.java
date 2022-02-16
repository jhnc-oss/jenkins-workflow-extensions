package io.jhnc.jenkins.plugins.workflow.queue;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ProjectBlockedPropertyTest {
    @Test
    void readResolveMigratesFields() throws IllegalAccessException {
        final ProjectBlockedProperty property = new ProjectBlockedProperty("", "");
        FieldUtils.writeDeclaredField(property, "message", null, true);
        FieldUtils.writeDeclaredField(property, "timestamp", null, true);
        FieldUtils.writeDeclaredField(property, "user", null, true);

        final Object result = property.readResolve();

        assertThat(property.getMessage()).isNotNull();
        assertThat(property.getTimestamp()).isNotNull();
        assertThat(property.getUser()).isNotNull();
        assertThat(property).isSameInstanceAs(result);
    }
}