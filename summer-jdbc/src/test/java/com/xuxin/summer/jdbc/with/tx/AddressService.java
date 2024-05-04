package com.xuxin.summer.jdbc.with.tx;

import java.util.List;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.Transactional;
import com.xuxin.summer.jdbc.JdbcTemplate;
import com.xuxin.summer.jdbc.JdbcTestBase;

@Component
@Transactional
public class AddressService {

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void addAddress(Address... addresses) {
        for (Address address : addresses) {
            // check if userId is existed:
            userService.getUser(address.userId);
            jdbcTemplate.update(JdbcTestBase.INSERT_ADDRESS, address.userId, address.address, address.zip);
        }
    }

    public List<Address> getAddresses(int userId) {
        return jdbcTemplate.queryForList(JdbcTestBase.SELECT_ADDRESS_BY_USERID, Address.class, userId);
    }

    public void deleteAddress(int userId) {
        jdbcTemplate.update(JdbcTestBase.DELETE_ADDRESS_BY_USERID, userId);
        if (userId == 1) {
            throw new RuntimeException("Rollback delete for user id = 1");
        }
    }
}
