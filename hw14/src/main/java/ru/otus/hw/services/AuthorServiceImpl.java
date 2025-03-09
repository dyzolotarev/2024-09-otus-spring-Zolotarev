package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorConverter authorConverter;

    @Override
    public Optional<AuthorDto> findById(String id) {
        return authorRepository.findById(id).map(authorConverter::authorToAuthorDto);
    }

    @Override
    public Optional<AuthorDto> findByFullName(String name) {
        return authorRepository.findByFullName(name).map(authorConverter::authorToAuthorDto);
    }

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(authorConverter::authorToAuthorDto).collect(Collectors.toList());
    }
}
