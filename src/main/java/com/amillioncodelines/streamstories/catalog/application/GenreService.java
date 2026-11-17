package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.GenreView;
import com.amillioncodelines.streamstories.catalog.domain.Genre;
import com.amillioncodelines.streamstories.catalog.domain.GenreRepository;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.catalog.domain.Slugs;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenreService {

    private final GenreRepository genres;
    private final MediaItemRepository titles;

    GenreService(GenreRepository genres, MediaItemRepository titles) {
        this.genres = genres;
        this.titles = titles;
    }

    @Transactional(readOnly = true)
    public Page<GenreView> list(Pageable pageable) {
        return genres.findAll(pageable).map(GenreView::of);
    }

    @Transactional(readOnly = true)
    public GenreView get(UUID id) {
        return GenreView.of(find(id));
    }

    /** Names and slugs are unique ignoring case (rule C3). */
    @Transactional
    public GenreView create(String name) {
        String slug = slugOf(name);
        if (genres.existsBySlug(slug)) {
            throw new ConflictException("Ya existe un género '%s'.".formatted(name.strip()));
        }
        return GenreView.of(genres.saveAndFlush(new Genre(name)));
    }

    @Transactional
    public GenreView rename(UUID id, String name) {
        Genre genre = find(id);
        String slug = slugOf(name);
        if (genres.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("Ya existe un género '%s'.".formatted(name.strip()));
        }
        genre.rename(name);
        return GenreView.of(genres.saveAndFlush(genre));
    }

    /** A genre that classifies titles cannot be deleted (rule C8). */
    @Transactional
    public void delete(UUID id) {
        Genre genre = find(id);
        if (titles.existsWithGenre(id)) {
            throw new ConflictException("El género '%s' tiene títulos asociados.".formatted(genre.getName()));
        }
        genres.delete(genre);
    }

    private static String slugOf(String name) {
        String slug = Slugs.from(name);
        if (slug.isEmpty()) {
            throw new InvalidRequestException("El nombre del género tiene que incluir letras o números.");
        }
        return slug;
    }

    private Genre find(UUID id) {
        return genres.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el género %s.".formatted(id)));
    }
}
