package com.api.calculator.stockprice.ws.data.model;

import com.api.calculator.stockprice.exceptions.NotAcceptedException;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NonNull;

import java.sql.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Entity(name = "operation")
@Data
public class Operation implements Comparable<Operation>, Cloneable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    @NotEmpty(message = "Forneça o nome do ativo")
    private String name;

    @NotEmpty(message = "Forneça o tipo do ativo: ATIVO ou OPÇÂO")
    private String activeType;

    @Positive(message = "A quantidade deve ser maior que zero.")
    private int qtd;

    private float value;

    @NotNull(message = "Forneça a data de liquidação da operação. Pode ser a data da realização da operação.")
    private Date date;

    @NotEmpty(message = "Forneça o tipo da operação: (DAY TRADE ou SWING TRADE)")
    private String typeOp;

    @NotEmpty(message = "Forneça o tipo de mercado: VISTA, FUTURO ou OPÇÕES..")
    private String typeMarket;

    private UUID fileId;

    private String wallet;

    private String closeMonth;

    private String noteNumber;

    public Operation(){

    }

    public Operation(Long id, UUID userId, String name, String activeType, Integer qtd, Float value, Date date, String typeOp,
                     String typeMarket, UUID fileId, String wallet, String closeMonth, String noteNumber){
        this.userId = userId;
        this.id = id;
        this.name = name;
        this.activeType = activeType;
        this.qtd = qtd;
        this.value = value;
        this.date = date;
        this.typeOp = typeOp;
        this.typeMarket = typeMarket;
        this.fileId = fileId;
        this.wallet = wallet;
        this.closeMonth = closeMonth;
        this.noteNumber = noteNumber;
    }

    public static Operation get(Map<String, Object> operationMap) throws NotAcceptedException {
        try {
            return new ObjectMapper().convertValue(operationMap, Operation.class);
        }catch (Exception e){
            throw new NotAcceptedException("Os valores fornecidos estão incorretos.");
        }
    }

    @Override
    public int compareTo(@NonNull Operation operation) {
        if(this.date == null || operation.date == null){
            return 0;
        }
        return this.date.compareTo(operation.getDate());
    }

    @Override
    public Operation clone() {
        try {
            return (Operation) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Operation clone: Clone object is no possible.");
            return new Operation();
        }
    }
}
