package com.back.global.initData

import com.back.global.app.AppConfig
import com.back.standard.util.Ut
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Profile("dev")
@Configuration
class DevInitData {

    @Bean
    fun devInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            Ut.cmd.runAsync(
                "npx{{DOT_CMD}}",
                "--yes",
                "--package", "typescript@v5",
                "--package", "openapi-typescript",
                "openapi-typescript", "${AppConfig.siteBackUrl}/v3/api-docs/apiV1",
                "-o", "../front/src/lib/backend/apiV1/schema.d.ts"
            )
        }
    }
}