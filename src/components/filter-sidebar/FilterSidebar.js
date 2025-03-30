import React from "react";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { format } from "date-fns";
import { ru } from "date-fns/locale";
import "./FilterSidebar.css";

const FilterSidebar = ({
  isOpen,
  toggleSidebar,
  searchTitle,
  setSearchTitle,
  searchDescription,
  setSearchDescription,
  selectedType,
  setSelectedType,
  typedAddress,
  setTypedAddress,
  selectedLocationId,
  setSelectedLocationId,
  selectedUserId,
  setSelectedUserId,
  dateRange,
  setDateRange,
  incidentTypes,
  locations,
  users,
  fetchIncidents,
  clearFilters
}) => {
  const [startDate, endDate] = dateRange;

  return (
    <>
      {/* Overlay when sidebar is open on mobile */}
      {isOpen && (
        <div className="filter-overlay" onClick={toggleSidebar}></div>
      )}

      <div className={`filter-sidebar ${isOpen ? "open" : ""}`}>
        <div className="filter-sidebar-header">
          <h3>Фильтры</h3>
          <button className="close-button" onClick={toggleSidebar}>
            ✕
          </button>
        </div>

        <div className="filter-sidebar-content">
          <div className="filter-group">
            <label>Название</label>
            <input
              type="text"
              placeholder="Название..."
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>Описание</label>
            <input
              type="text"
              placeholder="Описание..."
              value={searchDescription}
              onChange={(e) => setSearchDescription(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>Тип инцидента</label>
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
            >
              <option value="">Все типы</option>
              {incidentTypes.map((type) => (
                <option key={type.id} value={type.id}>
                  {type.name}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Место</label>
            <select
              value={selectedLocationId}
              onChange={(e) => {
                setSelectedLocationId(e.target.value);
                setTypedAddress("");
              }}
            >
              <option value="">Выбрать место</option>
              {locations.map((location) => (
                <option key={location.id} value={location.id}>
                  {location.address}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Адрес вручную</label>
            <input
              type="text"
              placeholder="Адрес вручную..."
              value={typedAddress}
              onChange={(e) => {
                setTypedAddress(e.target.value);
                setSelectedLocationId("");
              }}
            />
          </div>

          <div className="filter-group">
            <label>Пользователь</label>
            <select
              value={selectedUserId}
              onChange={(e) => setSelectedUserId(e.target.value)}
            >
              <option value="">Все пользователи</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.username || user.name || `User #${user.id}`}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Период</label>
            <DatePicker
              selectsRange={true}
              startDate={startDate}
              endDate={endDate}
              onChange={(update) => setDateRange(update)}
              dateFormat="dd.MM.yyyy"
              placeholderText="Выберите даты"
              className="date-picker"
              locale={ru}
              showMonthDropdown
              showYearDropdown
              dropdownMode="select"
            />
          </div>

          <div className="filter-buttons">
            <button className="search-button" onClick={fetchIncidents}>
              🔍 Поиск
            </button>
            <button className="clear-button" onClick={clearFilters}>
              ❌ Очистить
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default FilterSidebar;