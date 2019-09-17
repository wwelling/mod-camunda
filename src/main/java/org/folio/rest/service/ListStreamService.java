package org.folio.rest.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class ListStreamService extends AbstractStreamService<List<String>> {

    @Override
    public String orderedMergeFlux(String firstFluxId, Flux<List<String>> secondFlux, String comparisonProperty) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String enhanceFlux(String firstFluxId, Flux<List<String>> secondFlux, String comparisonProperty,
            String enhancementProperty) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}