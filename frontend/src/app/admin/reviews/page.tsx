'use client';

interface Review {
    id: number;
    userId: number;
    movieId: number | null;
    seriesId: number | null;
    rating: number;
    content: string;
}

const AdminReviewPage = () => {
    const [reviews, setReviews] = useState<Review[]>([]);
}