package jdroidcoder.ua.taxi_bishkek.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class OrderDto {
    private Long id;
    private String pointA;
    private String pointB;
    private Long time;
    private String userPhone;
    private String status;

    public OrderDto() {
    }

    public OrderDto(Long id, String pointA, String pointB, Long time, String userPhone, String status) {
        this.id = id;
        this.pointA = pointA;
        this.pointB = pointB;
        this.time = time;
        this.userPhone = userPhone;
        this.status = status;
    }

    public String getPointA() {
        return pointA;
    }

    public void setPointA(String pointA) {
        this.pointA = pointA;
    }

    public String getPointB() {
        return pointB;
    }

    public void setPointB(String pointB) {
        this.pointB = pointB;
    }

    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(time));
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPoints() {
        return getPointA() + " - " + getPointB();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderDto orderDto = (OrderDto) o;

        if (id != null ? !id.equals(orderDto.id) : orderDto.id != null) return false;
        if (pointA != null ? !pointA.equals(orderDto.pointA) : orderDto.pointA != null)
            return false;
        if (pointB != null ? !pointB.equals(orderDto.pointB) : orderDto.pointB != null)
            return false;
        if (time != null ? !time.equals(orderDto.time) : orderDto.time != null) return false;
        if (userPhone != null ? !userPhone.equals(orderDto.userPhone) : orderDto.userPhone != null)
            return false;
        return status != null ? status.equals(orderDto.status) : orderDto.status == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (pointA != null ? pointA.hashCode() : 0);
        result = 31 * result + (pointB != null ? pointB.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (userPhone != null ? userPhone.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "id=" + id +
                ", pointA='" + pointA + '\'' +
                ", pointB='" + pointB + '\'' +
                ", time=" + time +
                ", userPhone='" + userPhone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Oreders {
        private static List<OrderDto> orders = new ArrayList<>();

        public static void add(OrderDto orderDto) {
            orders.add(orderDto);
        }

        public static List<OrderDto> getOrders() {
            return orders;
        }

        public static void setItems(List<OrderDto> orders) {
            Oreders.orders = orders;
        }
    }
}
