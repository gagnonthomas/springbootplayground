package com.playground.service;

import com.playground.model.Item;
import com.playground.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemService service;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item("Widget", "A useful widget");
        sampleItem.setId(1L);
    }

    // --- findAll() ---

    @Test
    void findAll_returnsAllItemsFromRepository() {
        Item second = new Item("Gadget", "A cool gadget");
        second.setId(2L);
        when(repository.findAll()).thenReturn(List.of(sampleItem, second));

        List<Item> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(sampleItem, second);
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_returnsEmptyList_whenNoItemsExist() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<Item> result = service.findAll();

        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    // --- findById() ---

    @Test
    void findById_returnsItem_whenItemExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        Optional<Item> result = service.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Widget");
        assertThat(result.get().getDescription()).isEqualTo("A useful widget");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_returnsEmpty_whenItemDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Item> result = service.findById(99L);

        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(99L);
    }

    @Test
    void findById_passesIdToRepository_withoutModification() {
        Long targetId = 42L;
        when(repository.findById(targetId)).thenReturn(Optional.empty());

        service.findById(targetId);

        verify(repository).findById(42L);
    }

    // --- save() ---

    @Test
    void save_returnsPersistedItem_withGeneratedId() {
        Item newItem = new Item("Sprocket", "A small sprocket");
        Item savedItem = new Item("Sprocket", "A small sprocket");
        savedItem.setId(10L);
        when(repository.save(newItem)).thenReturn(savedItem);

        Item result = service.save(newItem);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Sprocket");
        assertThat(result.getDescription()).isEqualTo("A small sprocket");
        verify(repository, times(1)).save(newItem);
    }

    @Test
    void save_delegatesToRepository_withExactItemPassed() {
        when(repository.save(any(Item.class))).thenReturn(sampleItem);

        service.save(sampleItem);

        verify(repository).save(sampleItem);
    }

    @Test
    void save_returnsWhateverRepositoryReturns() {
        Item input = new Item("A", "B");
        Item repositoryResponse = new Item("X", "Y");
        repositoryResponse.setId(99L);
        when(repository.save(input)).thenReturn(repositoryResponse);

        Item result = service.save(input);

        assertThat(result).isSameAs(repositoryResponse);
    }

    // --- deleteById() ---

    @Test
    void deleteById_delegatesToRepository() {
        doNothing().when(repository).deleteById(1L);

        service.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_passesIdToRepository_withoutModification() {
        Long targetId = 7L;
        doNothing().when(repository).deleteById(targetId);

        service.deleteById(targetId);

        verify(repository).deleteById(7L);
    }

    @Test
    void deleteById_doesNotInteractWithOtherRepositoryMethods() {
        doNothing().when(repository).deleteById(1L);

        service.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(repository);
    }
}