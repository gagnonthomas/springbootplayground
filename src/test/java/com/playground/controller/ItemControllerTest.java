package com.playground.controller;

import com.playground.model.Item;
import com.playground.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ItemController using MockMvcTester with AssertJ assertions.
 *
 * Uses MockMvcTester.of() which requires no Spring context,
 * keeping tests fast and self-contained.
 */
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private MockMvcTester mockMvc;
    private ObjectMapper objectMapper;

    private Item sampleItem;
    private Item anotherItem;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTester.of(itemController);
        objectMapper = new ObjectMapper();

        sampleItem = new Item("Widget", "A useful widget");
        sampleItem.setId(1L);

        anotherItem = new Item("Gadget", "A cool gadget");
        anotherItem.setId(2L);
    }

    // =========================================================================
    // GET /api/items
    // =========================================================================

    @Test
    void getAll_returns200_withListOfItems() {
        when(itemService.findAll()).thenReturn(List.of(sampleItem, anotherItem));

        assertThat(mockMvc.get().uri("/api/items"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$").asArray()
                .hasSize(2);
    }

    @Test
    void getAll_returns200_withCorrectItemFields() {
        when(itemService.findAll()).thenReturn(List.of(sampleItem));

        MvcTestResult result = mockMvc.get().uri("/api/items").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].id").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$[0].name").isEqualTo("Widget");
        assertThat(result).bodyJson().extractingPath("$[0].description").isEqualTo("A useful widget");
    }

    @Test
    void getAll_returns200_withEmptyArray_whenNoItemsExist() {
        when(itemService.findAll()).thenReturn(Collections.emptyList());

        assertThat(mockMvc.get().uri("/api/items"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$").asArray()
                .isEmpty();
    }

    @Test
    void getAll_delegatesToService() {
        when(itemService.findAll()).thenReturn(Collections.emptyList());

        assertThat(mockMvc.get().uri("/api/items")).hasStatusOk();

        verify(itemService, times(1)).findAll();
    }

    // =========================================================================
    // GET /api/items/{id}
    // =========================================================================

    @Test
    void getById_returns200_withItem_whenItemExists() {
        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));

        MvcTestResult result = mockMvc.get().uri("/api/items/1").exchange();
        assertThat(result).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Widget");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("A useful widget");
    }

    @Test
    void getById_returns404_whenItemDoesNotExist() {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        assertThat(mockMvc.get().uri("/api/items/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void getById_returns404_forAnyNonexistentId() {
        when(itemService.findById(any())).thenReturn(Optional.empty());

        assertThat(mockMvc.get().uri("/api/items/1000"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void getById_passesCorrectIdToService() {
        when(itemService.findById(2L)).thenReturn(Optional.of(anotherItem));

        MvcTestResult result = mockMvc.get().uri("/api/items/2").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(2);

        verify(itemService).findById(2L);
    }

    // =========================================================================
    // POST /api/items
    // =========================================================================

    @Test
    void create_returns201_withCreatedItem() {
        Item requestBody = new Item("Widget", "A useful widget");
        when(itemService.save(any(Item.class))).thenReturn(sampleItem);

        MvcTestResult result = mockMvc.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestBody))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Widget");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("A useful widget");
    }

    @Test
    void create_callsSaveOnService_oncePer_request() {
        Item requestBody = new Item("NewItem", "New description");
        Item savedItem = new Item("NewItem", "New description");
        savedItem.setId(5L);
        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        MvcTestResult result = mockMvc.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestBody))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(5);

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void create_returnsFieldsFromServiceResponse() {
        Item requestBody = new Item("Sprocket", "A small sprocket");
        when(itemService.save(any(Item.class))).thenAnswer(invocation -> {
            Item arg = invocation.getArgument(0);
            arg.setId(3L);
            return arg;
        });

        MvcTestResult result = mockMvc.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestBody))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Sprocket");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("A small sprocket");
    }

    @Test
    void create_withNullNameAndDescription_stillCallsServiceAndReturns201() {
        Item requestBody = new Item(null, null);
        Item savedItem = new Item(null, null);
        savedItem.setId(4L);
        when(itemService.save(any(Item.class))).thenReturn(savedItem);

        MvcTestResult result = mockMvc.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestBody))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(4);
    }

    // =========================================================================
    // PUT /api/items/{id}
    // =========================================================================

    @Test
    void update_returns200_withUpdatedItem_whenItemExists() {
        Item updateRequest = new Item("Updated Widget", "Updated description");
        Item updatedItem = new Item("Updated Widget", "Updated description");
        updatedItem.setId(1L);

        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemService.save(any(Item.class))).thenReturn(updatedItem);

        MvcTestResult result = mockMvc.put().uri("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest))
                .exchange();
        assertThat(result).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Updated Widget");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("Updated description");
    }

    @Test
    void update_returns404_whenItemDoesNotExist() {
        Item updateRequest = new Item("Irrelevant", "Irrelevant");
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        assertThat(mockMvc.put().uri("/api/items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_appliesNewNameAndDescription_toExistingEntityBeforeSaving() {
        Item updateRequest = new Item("Renamed", "New desc");
        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemService.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MvcTestResult result = mockMvc.put().uri("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest))
                .exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Renamed");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("New desc");
    }

    @Test
    void update_doesNotCallSave_whenItemDoesNotExist() {
        Item updateRequest = new Item("Widget", "A useful widget");
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        assertThat(mockMvc.put().uri("/api/items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void update_preservesOriginalEntityId_ignoringIdFromRequestBody() {
        Item updateRequest = new Item("Renamed", "New desc");
        updateRequest.setId(999L);

        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemService.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MvcTestResult result = mockMvc.put().uri("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest))
                .exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(1);
    }

    // =========================================================================
    // DELETE /api/items/{id}
    // =========================================================================

    @Test
    void delete_returns204_whenItemExists() {
        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        doNothing().when(itemService).deleteById(1L);

        assertThat(mockMvc.delete().uri("/api/items/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void delete_returns404_whenItemDoesNotExist() {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        assertThat(mockMvc.delete().uri("/api/items/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_callsDeleteById_withCorrectId_whenItemExists() {
        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        doNothing().when(itemService).deleteById(1L);

        assertThat(mockMvc.delete().uri("/api/items/1"))
                .hasStatus(HttpStatus.NO_CONTENT);

        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void delete_doesNotCallDeleteById_whenItemDoesNotExist() {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        assertThat(mockMvc.delete().uri("/api/items/99"))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(itemService, never()).deleteById(any());
    }

    @Test
    void delete_responseBodyIsEmpty_onSuccessfulDelete() {
        when(itemService.findById(1L)).thenReturn(Optional.of(sampleItem));
        doNothing().when(itemService).deleteById(1L);

        assertThat(mockMvc.delete().uri("/api/items/1"))
                .hasStatus(HttpStatus.NO_CONTENT)
                .bodyText().isEmpty();
    }
}