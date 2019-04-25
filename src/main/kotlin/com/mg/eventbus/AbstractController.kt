package com.mg.eventbus

import com.mg.eventbus.inline.logger
import com.mg.eventbus.response.BaseResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.rest.webmvc.RepositoryLinksResource
import org.springframework.hateoas.ResourceProcessor
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@Slf4j
@RestController
abstract class AbstractController : ResourceProcessor<RepositoryLinksResource> {

    override fun process(resource: RepositoryLinksResource): RepositoryLinksResource = resource

    @Value("\${app.id}")
    private val instance: String? = null

    @GetMapping(value = ["/instanceId"])
    fun instanceId(): ResponseEntity<BaseResponse<String>> {
        val id = "{ \"running_instance_id\": \"$instance\"}"
        return ResponseEntity.ok(BaseResponse(status = 1, data = id))
    }

    @GetMapping(value = ["/rest"])
    @ResponseStatus(HttpStatus.OK)
    fun getLinks(): ResponseEntity<ResourceSupport> {
        val commands = ResourceSupport()
        commands.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this::class.java).instanceId()).withRel("instanceId").withType("GET"))
        createRestLinks()?.forEach {
            commands.add(ControllerLinkBuilder.linkTo(it.method).withRel(it.rel).withType(it.methodType).withTitle(it.title))
        }
        return ResponseEntity.ok(commands)
    }

    fun createRestLinks(): List<ControllerLink>? = null

    data class ControllerLink(val method: Any, val methodType: String, val rel: String, val title: String)

    companion object {
        val log = logger(this)
        const val POST = "POST"
        const val DELETE = "DELETE"
        const val GET = "GET"
        const val COMMAND = "command"
        const val QUERY = "query"
    }

}