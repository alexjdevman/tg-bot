package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для настроек данных пользователя и компании
 *
 * @author Aleksey Gorbachev
 */
public class AccountSettings {

    /**
     * Идентификатор организации
     */
    private Long organizationId;

    /**
     * ФИО HR-а
     */
    private String managerName;

    /**
     * Контактный телефон
     */
    private String phone;

    /**
     * Сайт компании
     */
    private String site;

    /**
     * Email HR-а
     */
    private String managerEmail;

    /**
     * Адрес компании (географический)
     */
    private String address;

    /**
     * Название компании
     */
    private String companyName;

    /**
     * Описание компании
     */
    private String companyDescription;

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }
}
