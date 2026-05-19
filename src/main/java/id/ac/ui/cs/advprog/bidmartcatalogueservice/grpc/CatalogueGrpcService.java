package id.ac.ui.cs.advprog.bidmartcatalogueservice.grpc;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.grpc.CatalogueServiceGrpc;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.grpc.GrpcGetListingSummaryRequest;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.grpc.GrpcGetListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CatalogueGrpcService extends CatalogueServiceGrpc.CatalogueServiceImplBase {

    private final ListingService listingService;

    public CatalogueGrpcService(ListingService listingService) {
        this.listingService = listingService;
    }

    @Override
    public void getListingSummary(GrpcGetListingSummaryRequest request, StreamObserver<GrpcGetListingSummaryResponse> responseObserver) {
        Listing listing = listingService.getListingById(request.getListingId());
        
        if (listing == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Listing not found with id: " + request.getListingId())
                    .asRuntimeException());
            return;
        }

        GrpcGetListingSummaryResponse response = GrpcGetListingSummaryResponse.newBuilder()
                .setId(listing.getId())
                .setSellerId(listing.getSellerId())
                .setStatus(listing.getStatus().name())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
