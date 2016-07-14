package com.vaadin.wscdn.client;

/**
 * Runtime widget set configuration interface.
 *
 * Service URL and usage scope can be specified, but Vaadin version is
 * automatically bound to <code>com.vaadin.shared.Version</code> at runtime.
 */
public interface WidgetSetConfiguration {

    /**
     * Use with all Vaadin UIs.
     *
     * This registers to VaadinService directly using default service URL.
     */
    void init();

    /**
     * Use the given service URL.
     *
     * @param serviceUrl
     */
    void init(String serviceUrl);

    /**
     * Add and add-on dependency to this Widget set.
     *
     * @param groupId Maven group id
     * @param artifactId Maven artifact id
     * @param version Maven version
     * @return Returns this instance
     */
    WidgetSetConfiguration addon(String groupId, String artifactId, String version);

    /**
     * Make this component to use "eager" loading mechanism.
     *
     * @param componentClassFqn Vaadin component that should be loaded eagerly to
     * client-side.
     *
     * @return Returns this instance
     */
    WidgetSetConfiguration eager(String componentClassFqn);

}
