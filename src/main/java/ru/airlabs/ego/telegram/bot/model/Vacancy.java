package ru.airlabs.ego.telegram.bot.model;

/**
 * Модель для передачи данных по вакансии
 *
 * @author Aleksey Gorbachev
 */
public class Vacancy {

    /**
     * Идентификатор вакансии
     */
    private Long id;

    /**
     * Номер вакансии в списке
     */
    private Integer number;

    /**
     * Название вакансии
     */
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(number);
        builder.append(". ");
        builder.append(name);
        builder.append(" (id = ");
        builder.append(id);
        builder.append(")");
        return builder.toString();
    }
}
