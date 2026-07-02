package com.brochure.cms.domain.contact;

import com.brochure.cms.shared.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/contacts")
public class AdminContactController {

    private final ContactService contactService;

    public AdminContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ApiResponse<List<ContactService.ContactDto>> list() {
        return ApiResponse.ok(contactService.listAll());
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<ContactService.ContactDto> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(contactService.markRead(id));
    }
}
