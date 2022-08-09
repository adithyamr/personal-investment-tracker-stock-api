package com.mavinasara.model.zerodha;

public class Pagination {

	private Long page;

	private Long per_page;

	private Long total_pages;

	private Long total;

	public Long getPage() {
		return page;
	}

	public void setPage(Long page) {
		this.page = page;
	}

	public Long getPer_page() {
		return per_page;
	}

	public void setPer_page(Long per_page) {
		this.per_page = per_page;
	}

	public Long getTotal_pages() {
		return total_pages;
	}

	public void setTotal_pages(Long total_pages) {
		this.total_pages = total_pages;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "Pagination [page=" + page + ", per_page=" + per_page + ", total_pages=" + total_pages + ", total="
				+ total + "]";
	}

}
