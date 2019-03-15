package com.mg.eventbus

import com.mg.eventbus.inline.logger
import lombok.extern.slf4j.Slf4j
import org.springframework.data.rest.webmvc.RepositoryLinksResource
import org.springframework.hateoas.ResourceProcessor


@Slf4j
abstract class AbstractController : ResourceProcessor<RepositoryLinksResource> {

    override fun process(resource: RepositoryLinksResource): RepositoryLinksResource = resource

    companion object {
        val log = logger(this)
    }

}