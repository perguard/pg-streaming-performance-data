/*
 * Copyright (c) 2021 fortiss - Research Institute of the Free State of Bavaria
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package pg.trace.collection.nagios;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

public class ServiceStatusDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)

    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("service_id")
    private String serviceId;
    @JsonProperty("host_id")
    private String hostId;
    @JsonProperty("host_name")
    private String hostName;
    @JsonProperty("host_alias")
    private String hostAlias;
    @JsonProperty("name")
    private String name;
    @JsonProperty("host_address")
    private String hostAddress;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("status_update_time")
    private String statusUpdateTime;
    @JsonProperty("status_text")
    private String statusText;
    @JsonProperty("current_state")
    private String currentState;
    @JsonProperty("performance_data")
    private String performanceData;
    @JsonProperty("should_be_scheduled")
    private String shouldBeScheduled;
    @JsonProperty("check_type")
    private String checkType;
    @JsonProperty("last_state_change")
    private String lastStateChange;
    @JsonProperty("last_hard_state_change")
    private String lastHardStateChange;
    @JsonProperty("last_hard_state")
    private String lastHardState;
    @JsonProperty("last_time_ok")
    private String lastTimeOk;
    @JsonProperty("last_time_warning")
    private String lastTimeWarning;
    @JsonProperty("last_time_critical")
    private String lastTimeCritical;
    @JsonProperty("last_time_unknown")
    private String lastTimeUnknown;
    @JsonProperty("last_notification")
    private String lastNotification;
    @JsonProperty("next_notification")
    private String nextNotification;
    @JsonProperty("no_more_notifications")
    private String noMoreNotifications;
    @JsonProperty("acknowledgement_type")
    private String acknowledgementType;
    @JsonProperty("current_notification_number")
    private String currentNotificationNumber;
    @JsonProperty("process_performance_data")
    private String processPerformanceData;
    @JsonProperty("obsess_over_service")
    private String obsessOverService;
    @JsonProperty("event_handler_enabled")
    private String eventHandlerEnabled;
    @JsonProperty("modified_service_attributes")
    private String modifiedServiceAttributes;
    @JsonProperty("check_command")
    private String checkCommand;
    @JsonProperty("normal_check_interval")
    private String normalCheckInterval;
    @JsonProperty("retry_check_interval")
    private String retryCheckInterval;
    @JsonProperty("check_timeperiod_id")
    private String checkTimeperiodId;
    @JsonProperty("has_been_checked")
    private String hasBeenChecked;
    @JsonProperty("current_check_attempt")
    private String currentCheckAttempt;
    @JsonProperty("max_check_attempts")
    private String maxCheckAttempts;
    @JsonProperty("last_check")
    private String lastCheck;
    @JsonProperty("next_check")
    private String nextCheck;
    @JsonProperty("state_type")
    private String stateType;
    @JsonProperty("notifications_enabled")
    private String notificationsEnabled;
    @JsonProperty("problem_acknowledged")
    private String problemAcknowledged;
    @JsonProperty("flap_detection_enabled")
    private String flapDetectionEnabled;
    @JsonProperty("is_flapping")
    private String isFlapping;
    @JsonProperty("percent_state_change")
    private String percentStateChange;
    @JsonProperty("latency")
    private String latency;
    @JsonProperty("execution_time")
    private String executionTime;
    @JsonProperty("scheduled_downtime_depth")
    private String scheduledDowntimeDepth;
    @JsonProperty("passive_checks_enabled")
    private String passiveChecksEnabled;
    @JsonProperty("active_checks_enabled")
    private String activeChecksEnabled;
    @JsonProperty("action_url")
    private Object actionUrl;
    @JsonProperty("event_handler")
    private Object eventHandler;
    @JsonProperty("icon_image_alt")
    private Object iconImageAlt;
    @JsonProperty("icon_image")
    private Object iconImage;
    @JsonProperty("host_display_name")
    private Object hostDisplayName;
    @JsonProperty("notes_url")
    private Object notesUrl;
    @JsonProperty("@attributes")
    private Object attributes;
    @JsonProperty("status_text_long")
    private Object statusTextLong;





    public ServiceStatusDTO() {

    }

    @JsonProperty("instance_id")
    public String getInstanceId() {
        return instanceId;
    }

    @JsonProperty("instance_id")
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @JsonProperty("service_id")
    public String getServiceId() {
        return serviceId;
    }

    @JsonProperty("service_id")
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @JsonProperty("host_id")
    public String getHostId() {
        return hostId;
    }

    @JsonProperty("host_id")
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    @JsonProperty("host_name")
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("host_name")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @JsonProperty("host_alias")
    public String getHostAlias() {
        return hostAlias;
    }

    @JsonProperty("host_alias")
    public void setHostAlias(String hostAlias) {
        this.hostAlias = hostAlias;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("host_address")
    public String getHostAddress() {
        return hostAddress;
    }

    @JsonProperty("host_address")
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("status_update_time")
    public String getStatusUpdateTime() {
        return statusUpdateTime;
    }

    @JsonProperty("status_update_time")
    public void setStatusUpdateTime(String statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    @JsonProperty("status_text")
    public String getStatusText() {
        return statusText;
    }

    @JsonProperty("status_text")
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    @JsonProperty("current_state")
    public String getCurrentState() {
        return currentState;
    }

    @JsonProperty("current_state")
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    @JsonProperty("performance_data")
    public String getPerformanceData() {
        performanceData = performanceData.replaceAll("[^0-9.]", "");
        return performanceData;
    }

    @JsonProperty("performance_data")
    public void setPerformanceData(JsonNode performanceData) {
        this.performanceData = performanceData.asText();
    }

    @JsonProperty("should_be_scheduled")
    public String getShouldBeScheduled() {
        return shouldBeScheduled;
    }

    @JsonProperty("should_be_scheduled")
    public void setShouldBeScheduled(String shouldBeScheduled) {
        this.shouldBeScheduled = shouldBeScheduled;
    }

    @JsonProperty("check_type")
    public String getCheckType() {
        return checkType;
    }

    @JsonProperty("check_type")
    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    @JsonProperty("last_state_change")
    public String getLastStateChange() {
        return lastStateChange;
    }

    @JsonProperty("last_state_change")
    public void setLastStateChange(String lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    @JsonProperty("last_hard_state_change")
    public String getLastHardStateChange() {
        return lastHardStateChange;
    }

    @JsonProperty("last_hard_state_change")
    public void setLastHardStateChange(String lastHardStateChange) {
        this.lastHardStateChange = lastHardStateChange;
    }

    @JsonProperty("last_hard_state")
    public String getLastHardState() {
        return lastHardState;
    }

    @JsonProperty("last_hard_state")
    public void setLastHardState(String lastHardState) {
        this.lastHardState = lastHardState;
    }

    @JsonProperty("last_time_ok")
    public String getLastTimeOk() {
        return lastTimeOk;
    }

    @JsonProperty("last_time_ok")
    public void setLastTimeOk(String lastTimeOk) {
        this.lastTimeOk = lastTimeOk;
    }

    @JsonProperty("last_time_warning")
    public String getLastTimeWarning() {
        return lastTimeWarning;
    }

    @JsonProperty("last_time_warning")
    public void setLastTimeWarning(String lastTimeWarning) {
        this.lastTimeWarning = lastTimeWarning;
    }

    @JsonProperty("last_time_critical")
    public String getLastTimeCritical() {
        return lastTimeCritical;
    }

    @JsonProperty("last_time_critical")
    public void setLastTimeCritical(String lastTimeCritical) {
        this.lastTimeCritical = lastTimeCritical;
    }

    @JsonProperty("last_time_unknown")
    public String getLastTimeUnknown() {
        return lastTimeUnknown;
    }

    @JsonProperty("last_time_unknown")
    public void setLastTimeUnknown(String lastTimeUnknown) {
        this.lastTimeUnknown = lastTimeUnknown;
    }

    @JsonProperty("last_notification")
    public String getLastNotification() {
        return lastNotification;
    }

    @JsonProperty("last_notification")
    public void setLastNotification(String lastNotification) {
        this.lastNotification = lastNotification;
    }

    @JsonProperty("next_notification")
    public String getNextNotification() {
        return nextNotification;
    }

    @JsonProperty("next_notification")
    public void setNextNotification(String nextNotification) {
        this.nextNotification = nextNotification;
    }

    @JsonProperty("no_more_notifications")
    public String getNoMoreNotifications() {
        return noMoreNotifications;
    }

    @JsonProperty("no_more_notifications")
    public void setNoMoreNotifications(String noMoreNotifications) {
        this.noMoreNotifications = noMoreNotifications;
    }

    @JsonProperty("acknowledgement_type")
    public String getAcknowledgementType() {
        return acknowledgementType;
    }

    @JsonProperty("acknowledgement_type")
    public void setAcknowledgementType(String acknowledgementType) {
        this.acknowledgementType = acknowledgementType;
    }

    @JsonProperty("current_notification_number")
    public String getCurrentNotificationNumber() {
        return currentNotificationNumber;
    }

    @JsonProperty("current_notification_number")
    public void setCurrentNotificationNumber(String currentNotificationNumber) {
        this.currentNotificationNumber = currentNotificationNumber;
    }

    @JsonProperty("process_performance_data")
    public String getProcessPerformanceData() {
        return processPerformanceData;
    }

    @JsonProperty("process_performance_data")
    public void setProcessPerformanceData(String processPerformanceData) {
        this.processPerformanceData = processPerformanceData;
    }

    @JsonProperty("obsess_over_service")
    public String getObsessOverService() {
        return obsessOverService;
    }

    @JsonProperty("obsess_over_service")
    public void setObsessOverService(String obsessOverService) {
        this.obsessOverService = obsessOverService;
    }

    @JsonProperty("event_handler_enabled")
    public String getEventHandlerEnabled() {
        return eventHandlerEnabled;
    }

    @JsonProperty("event_handler_enabled")
    public void setEventHandlerEnabled(String eventHandlerEnabled) {
        this.eventHandlerEnabled = eventHandlerEnabled;
    }

    @JsonProperty("modified_service_attributes")
    public String getModifiedServiceAttributes() {
        return modifiedServiceAttributes;
    }

    @JsonProperty("modified_service_attributes")
    public void setModifiedServiceAttributes(String modifiedServiceAttributes) {
        this.modifiedServiceAttributes = modifiedServiceAttributes;
    }

    @JsonProperty("check_command")
    public String getCheckCommand() {
        return checkCommand;
    }

    @JsonProperty("check_command")
    public void setCheckCommand(String checkCommand) {
        this.checkCommand = checkCommand;
    }

    @JsonProperty("normal_check_interval")
    public String getNormalCheckInterval() {
        return normalCheckInterval;
    }

    @JsonProperty("normal_check_interval")
    public void setNormalCheckInterval(String normalCheckInterval) {
        this.normalCheckInterval = normalCheckInterval;
    }

    @JsonProperty("retry_check_interval")
    public String getRetryCheckInterval() {
        return retryCheckInterval;
    }

    @JsonProperty("retry_check_interval")
    public void setRetryCheckInterval(String retryCheckInterval) {
        this.retryCheckInterval = retryCheckInterval;
    }

    @JsonProperty("check_timeperiod_id")
    public String getCheckTimeperiodId() {
        return checkTimeperiodId;
    }

    @JsonProperty("check_timeperiod_id")
    public void setCheckTimeperiodId(String checkTimeperiodId) {
        this.checkTimeperiodId = checkTimeperiodId;
    }

    @JsonProperty("has_been_checked")
    public String getHasBeenChecked() {
        return hasBeenChecked;
    }

    @JsonProperty("has_been_checked")
    public void setHasBeenChecked(String hasBeenChecked) {
        this.hasBeenChecked = hasBeenChecked;
    }

    @JsonProperty("current_check_attempt")
    public String getCurrentCheckAttempt() {
        return currentCheckAttempt;
    }

    @JsonProperty("current_check_attempt")
    public void setCurrentCheckAttempt(String currentCheckAttempt) {
        this.currentCheckAttempt = currentCheckAttempt;
    }

    @JsonProperty("max_check_attempts")
    public String getMaxCheckAttempts() {
        return maxCheckAttempts;
    }

    @JsonProperty("max_check_attempts")
    public void setMaxCheckAttempts(String maxCheckAttempts) {
        this.maxCheckAttempts = maxCheckAttempts;
    }

    @JsonProperty("last_check")
    public String getLastCheck() {
        return lastCheck;
    }

    @JsonProperty("last_check")
    public void setLastCheck(String lastCheck) {
        this.lastCheck = lastCheck;
    }

    @JsonProperty("next_check")
    public String getNextCheck() {
        return nextCheck;
    }

    @JsonProperty("next_check")
    public void setNextCheck(String nextCheck) {
        this.nextCheck = nextCheck;
    }

    @JsonProperty("state_type")
    public String getStateType() {
        return stateType;
    }

    @JsonProperty("state_type")
    public void setStateType(String stateType) {
        this.stateType = stateType;
    }

    @JsonProperty("notifications_enabled")
    public String getNotificationsEnabled() {
        return notificationsEnabled;
    }

    @JsonProperty("notifications_enabled")
    public void setNotificationsEnabled(String notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    @JsonProperty("problem_acknowledged")
    public String getProblemAcknowledged() {
        return problemAcknowledged;
    }

    @JsonProperty("problem_acknowledged")
    public void setProblemAcknowledged(String problemAcknowledged) {
        this.problemAcknowledged = problemAcknowledged;
    }

    @JsonProperty("flap_detection_enabled")
    public String getFlapDetectionEnabled() {
        return flapDetectionEnabled;
    }

    @JsonProperty("flap_detection_enabled")
    public void setFlapDetectionEnabled(String flapDetectionEnabled) {
        this.flapDetectionEnabled = flapDetectionEnabled;
    }

    @JsonProperty("is_flapping")
    public String getIsFlapping() {
        return isFlapping;
    }

    @JsonProperty("is_flapping")
    public void setIsFlapping(String isFlapping) {
        this.isFlapping = isFlapping;
    }

    @JsonProperty("percent_state_change")
    public String getPercentStateChange() {
        return percentStateChange;
    }

    @JsonProperty("percent_state_change")
    public void setPercentStateChange(String percentStateChange) {
        this.percentStateChange = percentStateChange;
    }

    @JsonProperty("latency")
    public String getLatency() {
        return latency;
    }

    @JsonProperty("latency")
    public void setLatency(String latency) {
        this.latency = latency;
    }

    @JsonProperty("execution_time")
    public String getExecutionTime() {
        return executionTime;
    }

    @JsonProperty("execution_time")
    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    @JsonProperty("scheduled_downtime_depth")
    public String getScheduledDowntimeDepth() {
        return scheduledDowntimeDepth;
    }

    @JsonProperty("scheduled_downtime_depth")
    public void setScheduledDowntimeDepth(String scheduledDowntimeDepth) {
        this.scheduledDowntimeDepth = scheduledDowntimeDepth;
    }

    @JsonProperty("passive_checks_enabled")
    public String getPassiveChecksEnabled() {
        return passiveChecksEnabled;
    }

    @JsonProperty("passive_checks_enabled")
    public void setPassiveChecksEnabled(String passiveChecksEnabled) {
        this.passiveChecksEnabled = passiveChecksEnabled;
    }

    @JsonProperty("active_checks_enabled")
    public String getActiveChecksEnabled() {
        return activeChecksEnabled;
    }

    @JsonProperty("active_checks_enabled")
    public void setActiveChecksEnabled(String activeChecksEnabled) {
        this.activeChecksEnabled = activeChecksEnabled;
    }

    @JsonProperty("action_url")
    public Object getActionUrl() {
        return actionUrl;
    }

    @JsonProperty("action_url")
    public void setActionUrl(Object actionUrl) {
        this.actionUrl = actionUrl;
    }


    @JsonProperty("event_handler")
    public void setEventHandler(Object eventHandler) {
        this.eventHandler = eventHandler;
    }

    @JsonProperty("event_handler")
    public Object getEventHandler() {
        return eventHandler;
    }

    @JsonProperty("icon_image_alt")
    public void setIconImageAlt(Object iconImageAlt) {
        this.iconImageAlt = iconImageAlt;
    }

    @JsonProperty("icon_image_alt")
    public Object getIconImageAlt() {
        return this.iconImageAlt;
    }

    @JsonProperty("icon_image")
    public void setIconImage(Object iconImage) {
        this.iconImage = iconImage;
    }

    @JsonProperty("icon_image")
    public Object getIconImage() {
        return this.iconImage;
    }

    @JsonProperty("host_display_name")
    public void setHostDisplayName(Object hostDisplayName){
        this.hostDisplayName = hostDisplayName;
    }

    @JsonProperty("host_display_name")
    public Object getHostDisplayName(){
        return this.hostDisplayName;
    }

    @JsonProperty("notes_url")
    public Object getNotesUrl() {
        return notesUrl;
    }

    @JsonProperty("notes_url")
    public void setNotesUrl(Object notesUrl) {
        this.notesUrl = notesUrl;
    }

    @JsonProperty("@attributes")
    public Object getAttributes() {
        return attributes;
    }

    @JsonProperty("@attributes")
    public void setAtrributes(Object attributes) {
        this.attributes = attributes;
    }

    @JsonProperty("status_text_long")
    public Object getStatusTextLong() {
        return statusTextLong;
    }

    @JsonProperty("status_text_long")
    public void setStatusTextLong(Object statusTextLong) {
        this.statusTextLong = statusTextLong;
    }
}
