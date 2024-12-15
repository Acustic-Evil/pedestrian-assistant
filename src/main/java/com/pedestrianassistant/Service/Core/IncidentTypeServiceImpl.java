package com.pedestrianassistant.Service.Core;

import com.pedestrianassistant.Model.Core.IncidentType;
import com.pedestrianassistant.Repository.Core.IncidentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentTypeServiceImpl implements IncidentTypeService {

    private final IncidentTypeRepository incidentTypeRepository;

    @Autowired
    public IncidentTypeServiceImpl(IncidentTypeRepository incidentTypeRepository) {
        this.incidentTypeRepository = incidentTypeRepository;
    }

    @Override
    public List<IncidentType> findAll() {
        return incidentTypeRepository.findAll();
    }

    @Override
    public Optional<IncidentType> findById(Long id) {
        return incidentTypeRepository.findById(id);
    }

    @Override
    public IncidentType save(IncidentType incidentType) {
        return incidentTypeRepository.save(incidentType);
    }

    @Override
    public void deleteById(Long id) {
        incidentTypeRepository.deleteById(id);
    }
}
