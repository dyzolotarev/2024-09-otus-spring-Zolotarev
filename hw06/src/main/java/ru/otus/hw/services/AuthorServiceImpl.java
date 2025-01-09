package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    private final AuthorConverter authorConverter;

    @Override
    public Optional<AuthorDto> findById(long id) {
        return authorRepository.findById(id).map(authorConverter::authorToAuthorDto);
    }

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(authorConverter::authorToAuthorDto).collect(Collectors.toList());
    }
}
