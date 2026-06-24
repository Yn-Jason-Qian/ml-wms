package com.wms.inventory.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock();
        stock.setQtyOnHand(BigDecimal.valueOf(100));
        stock.setQtyAllocated(BigDecimal.ZERO);
        stock.setQtyAvailable(BigDecimal.valueOf(100));
        stock.setQtyFrozen(BigDecimal.ZERO);
    }

    @Test
    void testAddStock() {
        stock.add(BigDecimal.valueOf(50));
        assertEquals(BigDecimal.valueOf(150), stock.getQtyOnHand());
        assertEquals(BigDecimal.valueOf(150), stock.getQtyAvailable());
        assertNotNull(stock.getLastInTime());
    }

    @Test
    void testDeductStock() {
        stock.allocate(BigDecimal.valueOf(30));
        assertEquals(BigDecimal.valueOf(30), stock.getQtyAllocated());
        assertEquals(BigDecimal.valueOf(70), stock.getQtyAvailable());

        stock.deduct(BigDecimal.valueOf(30));
        assertEquals(BigDecimal.valueOf(70), stock.getQtyOnHand());
        assertEquals(BigDecimal.valueOf(0), stock.getQtyAllocated());
        assertEquals(BigDecimal.valueOf(70), stock.getQtyAvailable());
    }

    @Test
    void testDeductMoreThanOnHandShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> stock.deduct(BigDecimal.valueOf(200)));
    }

    @Test
    void testAllocateMoreThanAvailableShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> stock.allocate(BigDecimal.valueOf(200)));
    }

    @Test
    void testFreezeStock() {
        stock.freeze(BigDecimal.valueOf(40));
        assertEquals(BigDecimal.valueOf(40), stock.getQtyFrozen());
        assertEquals(BigDecimal.valueOf(60), stock.getQtyAvailable());
        assertEquals(BigDecimal.valueOf(100), stock.getQtyOnHand());
    }

    @Test
    void testFreezeMoreThanAvailableShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> stock.freeze(BigDecimal.valueOf(200)));
    }

    @Test
    void testUnfreezeStock() {
        stock.freeze(BigDecimal.valueOf(40));
        stock.unfreeze(BigDecimal.valueOf(30));
        assertEquals(BigDecimal.valueOf(10), stock.getQtyFrozen());
        assertEquals(BigDecimal.valueOf(90), stock.getQtyAvailable());
    }

    @Test
    void testUnfreezeMoreThanFrozenShouldFail() {
        stock.freeze(BigDecimal.valueOf(10));
        assertThrows(IllegalArgumentException.class, () -> stock.unfreeze(BigDecimal.valueOf(50)));
    }

    @Test
    void testIsEmpty() {
        assertFalse(stock.isEmpty());
        stock.deduct(BigDecimal.valueOf(100));
        stock.setQtyOnHand(BigDecimal.ZERO);
        assertTrue(stock.isEmpty());
    }

    @Test
    void testDeallocate() {
        stock.allocate(BigDecimal.valueOf(20));
        stock.deallocate(BigDecimal.valueOf(15));
        assertEquals(BigDecimal.valueOf(5), stock.getQtyAllocated());
        assertEquals(BigDecimal.valueOf(95), stock.getQtyAvailable());
    }

    @Test
    void testDeallocateMoreThanAllocatedShouldFail() {
        stock.allocate(BigDecimal.valueOf(10));
        assertThrows(
                IllegalArgumentException.class, () -> stock.deallocate(BigDecimal.valueOf(50)));
    }
}
