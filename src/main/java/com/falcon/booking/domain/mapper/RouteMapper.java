package com.falcon.booking.domain.mapper;

import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.web.dto.Route.ResponseRouteDto;
import com.falcon.booking.web.dto.Route.CreateRouteDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteEntity toEntity(CreateRouteDto createRouteDto);
    ResponseRouteDto toResponseDto(RouteEntity route);
    List<ResponseRouteDto> toResponseDto(List<RouteEntity> route);
}
