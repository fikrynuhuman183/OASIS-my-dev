package com.oasis.ocrspring.service;

import com.oasis.ocrspring.model.Option;
import com.oasis.ocrspring.repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptionService {
    private final OptionRepository optionRepo;

    @Autowired
    public OptionService(OptionRepository optionRepo) {
        this.optionRepo = optionRepo;
    }

    public List<Option> allOptionDetails() {
        return optionRepo.findAll();
    }
    public Option findByName(String name) {
        String regexName = name.replace(" ", "\\s*");
        return optionRepo.findByNameRegex(regexName);
    }
    public void saveOption(Option option) {
        optionRepo.save(option);
    }
}
