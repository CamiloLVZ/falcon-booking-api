package com.falcon.booking.feature.route.mapper;

import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteEntity toEntity(CreateRouteDto createRouteDto);
    ResponseRouteDto toResponseDto(RouteEntity route);
    List<ResponseRouteDto> toResponseDto(List<RouteEntity> route);
}
