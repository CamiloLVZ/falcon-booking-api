package com.falcon.booking.domain.common.converter;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AirplaneTypeStatusConverter implements Converter<String, AirplaneTypeStatus> {

    @Override
    public AirplaneTypeStatus convert(String source) {
        if(source == null){
            return null;
        }
        return AirplaneTypeStatus.valueOf(source.trim().toUpperCase());
    }

}