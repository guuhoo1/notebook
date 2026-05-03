package com.notebook.dto;

import java.util.List;

public class CategorySortDTO {
    private List<CategorySortItem> orders;

    public CategorySortDTO() {
    }

    public List<CategorySortItem> getOrders() {
        return orders;
    }

    public void setOrders(List<CategorySortItem> orders) {
        this.orders = orders;
    }

    public static class CategorySortItem {
        private Long id;
        private Integer sortOrder;

        public CategorySortItem() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
