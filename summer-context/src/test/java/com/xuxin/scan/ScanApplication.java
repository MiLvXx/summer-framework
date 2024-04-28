package com.xuxin.scan;

import com.xuxin.imported.LocalDateConfiguration;
import com.xuxin.imported.ZonedDateConfiguration;
import com.xuxin.summer.annotation.ComponentScan;
import com.xuxin.summer.annotation.Import;

@ComponentScan
@Import({ LocalDateConfiguration.class, ZonedDateConfiguration.class})
public class ScanApplication {
    
}
