package com.xuxin.summer.jdbc.with.tx;

import com.xuxin.summer.annotation.ComponentScan;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Import;
import com.xuxin.summer.jdbc.JdbcConfiguration;

@ComponentScan
@Configuration
@Import(JdbcConfiguration.class)
public class JdbcWithTxApplication {

}
