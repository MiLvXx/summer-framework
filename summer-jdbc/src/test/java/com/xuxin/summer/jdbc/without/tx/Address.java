package com.xuxin.summer.jdbc.without.tx;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/3
 */
public class Address {

    public int id;
    public int userId;
    public String address;
    public int zipcode;

    public void setZip(Integer zip) {
        this.zipcode = zip == null ? 0 : zip;
    }
}
